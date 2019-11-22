package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.db.dao.AchievementsDao
import com.crepetete.steamachievements.db.dao.GamesDao
import com.crepetete.steamachievements.repository.limiter.RateLimiter
import com.crepetete.steamachievements.repository.resource.LiveResource
import com.crepetete.steamachievements.repository.resource.NetworkBoundResource
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.BaseGameInfo
import com.crepetete.steamachievements.vo.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class GameRepository @Inject constructor(
    private val achievementsDao: AchievementsDao,
    private val api: SteamApiService,
    private val gamesDao: GamesDao
) {

    private companion object {
        const val FETCH_GAMES_KEY = "FETCH_GAMES_KEY"
    }

    // Refresh games every day
    private val rateLimiter = RateLimiter<String>(1, TimeUnit.DAYS)

    /**
     * Fetch Games from both the Database and the API.
     * Refresh rate is set with the [rateLimiter].
     */
    fun getGames(userId: String): LiveResource<List<Game>> {
        return object : NetworkBoundResource<List<Game>, List<Game>>() {

            override suspend fun saveCallResult(data: List<Game>) {
                achievementsDao.upsert(data.flatMap { game -> game.achievements })
            }

            override fun shouldFetch(data: List<Game>?): Boolean {
                return rateLimiter.shouldFetch(FETCH_GAMES_KEY) || data == null
            }

            override suspend fun createCall(): List<Game>? {
                val games: MutableList<Game> = mutableListOf()
                val gamesResponse = api.getGamesForUser(userId).response.games

                gamesResponse?.let { baseGameInfo ->
                    gamesDao.upsert(baseGameInfo)

                    baseGameInfo.forEach { baseGame ->
                        val achievements =
                            if (rateLimiter.shouldFetch("achievements_${baseGame.appId}")) {
                                fetchAchievementsFromApi(userId, baseGame.appId.toString())
                            } else {
                                achievementsDao.getAchievements(baseGame.appId.toString())
                            }

                        games.add(Game(baseGame, achievements ?: listOf()))
                    }
                }
                return games
            }

            override suspend fun loadFromDb(): List<Game> {
                return gamesDao.getGames()
            }
        }.asLiveResource()
    }

    suspend fun fetchAchievementsFromApi(userId: String, appId: String): List<Achievement>? {
        try {
            val baseResponse = api.getSchemaForGame(appId)

            /* Reference to base achievements list. */
            val responseAchievements = baseResponse.game.availableGameStats?.achievements

            /* Iterate over all object in the response.  */
            if (responseAchievements?.isNotEmpty() == true) {
                try {
                    val achievedResponse = api.getAchievementsForPlayer(appId, userId)

                    achievedResponse.playerStats?.achievements?.forEach { response ->
                        /* For each one, find the corresponding achievement in the
                           responseAchievements list and update the information. */
                        responseAchievements.filter { achievement ->
                            achievement.name == response.apiName
                        }.forEach { resultAchievement ->
                            resultAchievement.appId = appId.toLong()
                            resultAchievement.unlockTime = response.getUnlockDate()
                            resultAchievement.achieved = response.achieved != 0
                            response.description?.let { desc ->
                                resultAchievement.description = desc
                            }
                        }
                    }

                    val globalResponse = api.getGlobalAchievementStats(appId)

                    globalResponse.achievementpercentages.achievements.forEach { response ->
                        responseAchievements.filter { achievement ->
                            achievement.name == response.name
                        }
                            .forEach { game ->
                                game.percentage = response.percent
                            }
                    }
                } catch (e: Exception) {
                    Timber.d(e)
                }
            }
            return responseAchievements
        } catch (e: Exception) {
            Timber.d(e)
            return null
        }
    }

    /**
     * Fetch a specific [Game] form the Database.
     * Steam API doesn't provide a call for one specific game, so the data will only be the stored
     * value of the last API call.
     */
    fun getGame(appId: String): LiveData<Game> {
        return liveData {
            emitSource(gamesDao.getGame(appId))
        }
    }

    fun update(item: BaseGameInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            gamesDao.upsert(item)
        }
    }
}