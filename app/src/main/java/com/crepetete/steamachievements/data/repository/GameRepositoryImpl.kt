package com.crepetete.steamachievements.data.repository

import androidx.lifecycle.LiveData
import com.crepetete.data.helper.LiveResource
import com.crepetete.data.helper.NetworkBoundResource
import com.crepetete.data.helper.RateLimiter
import com.crepetete.data.network.SteamApiService
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.database.dao.NewsDao
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.domain.repository.GameRepository
import com.crepetete.steamachievements.testing.OpenForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

@OpenForTesting
class GameRepositoryImpl(
    private val achievementsDao: AchievementsDao,
    private val api: SteamApiService,
    private val gamesDao: GamesDao,
    private val newsDao: NewsDao
) : GameRepository {

    private companion object {
        const val FETCH_GAMES_KEY = "FETCH_GAMES_KEY"
        const val FETCH_NEWS_KEY = "FETCH_NEWS_KEY"
    }

    // Refresh games every day
    private val rateLimiter = RateLimiter<String>(1, TimeUnit.DAYS)

    /**
     * Fetch Games from both the Database and the API.
     * Refresh rate is set with the [rateLimiter].
     */
    override fun getGames(userId: String?): LiveResource<List<Game>> {
        return object : NetworkBoundResource<List<Game>, List<Game>>() {

            override suspend fun saveCallResult(data: List<Game>) {
            }

            override fun shouldFetch(data: List<Game>?): Boolean {
                return rateLimiter.shouldFetch(FETCH_GAMES_KEY) || data == null
            }

            override suspend fun createCall(): List<Game>? {
                val games: MutableList<Game> = mutableListOf()
                val gamesResponse = if (userId != null) {
                    api.getGamesForUser(userId).response.games
                } else {
                    listOf()
                }

                gamesResponse?.let { baseGameInfo ->
                    gamesDao.upsert(baseGameInfo)

                    baseGameInfo.forEach { baseGame ->
                        val achievements =
                            if (rateLimiter.shouldFetch("achievements_${baseGame.appId}") && userId != null) {
                                fetchAchievementsFromApi(userId, baseGame.appId.toString())
                            } else {
                                achievementsDao.getAchievements(baseGame.appId.toString())
                            }

                        achievementsDao.upsert(achievements ?: listOf())
                        games.add(
                            Game(
                                baseGame,
                                achievements ?: listOf()
                            )
                        )
                    }
                }
                return games
            }

            override suspend fun loadFromDb(): List<Game> {
                return gamesDao.getGames()
            }
        }.asLiveResource()
    }

    override fun fetchAchievementsFromApi(userId: String, appId: String): List<Achievement>? {
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
    override fun getGame(appId: String): LiveData<Game> {
        return gamesDao.getGame(appId)
    }

    override fun update(item: BaseGameInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            gamesDao.upsert(item)
        }
    }

    override fun getNews(appId: String): LiveResource<List<NewsItem>> {
        return object : NetworkBoundResource<List<NewsItem>, List<NewsItem>?>() {
            override suspend fun saveCallResult(data: List<NewsItem>?) {
                data?.let { news ->
                    newsDao.upsert(news)
                }
            }

            override fun shouldFetch(data: List<NewsItem>?): Boolean {
                return data == null || rateLimiter.shouldFetch(FETCH_NEWS_KEY)
            }

            override suspend fun loadFromDb(): List<NewsItem>? {
                return newsDao.getNewsForGame(appId)?.takeLast(3)
            }

            override suspend fun createCall(): List<NewsItem>? {
                return api.getNews(appId).appNews.newsItems
            }
        }.asLiveResource()
    }
}