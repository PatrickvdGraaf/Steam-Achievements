package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.ApiResponse
import com.crepetete.steamachievements.api.response.game.BaseGameResponse
import com.crepetete.steamachievements.db.dao.GamesDao
import com.crepetete.steamachievements.repository.limiter.RateLimiter
import com.crepetete.steamachievements.repository.resource.NetworkBoundResource
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.util.livedata.AbsentLiveData
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.crepetete.steamachievements.vo.Resource
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class GameRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val dao: GamesDao,
    private val api: SteamApiService
) {

    // Refresh games every day
    private val gameListRateLimit = RateLimiter<String>(1, TimeUnit.DAYS)

    fun getGames(userId: String): LiveData<Resource<List<GameWithAchievements>>> {
        return object : NetworkBoundResource<List<GameWithAchievements>, BaseGameResponse>(appExecutors) {

            override fun saveCallResult(item: BaseGameResponse) {
                Timber.d("Saving Games in DB")

                val games = item.response.games
                games.forEach { game ->
                    game.userId = userId

                    if (game.name.contains("Planet Coaster", true)){
                        Timber.d(game.recentPlayTime.toString())
                    }
                }

                dao.upsert(games)
            }

            override fun shouldFetch(data: List<GameWithAchievements>?) = data == null
                || gameListRateLimit.shouldFetch("getGamesForUser$userId")

            override fun loadFromDb(): LiveData<List<GameWithAchievements>> = dao.getGamesWithAchievementsAsLiveData()

            override fun createCall(): LiveData<ApiResponse<BaseGameResponse>> = api.getGamesForUser(userId)

            override fun onFetchFailed() {
                gameListRateLimit.reset(userId)
                if (userId == "-1") {
                    Timber.e("An invalid userId was passed to GameRepository.getGames(). Are the SP empty?")
                }
            }
        }.asLiveData()
    }

    fun getGame(appId: String): LiveData<Resource<GameWithAchievements>> {
        return object : NetworkBoundResource<GameWithAchievements, BaseGameResponse>(appExecutors) {

            override fun saveCallResult(item: BaseGameResponse) {
                Timber.d("Saving Games in DB")

                val games = item.response.games
                games.forEach { game ->
                    game.userId = appId
                }

                dao.upsert(games)
            }

            // At the moment, Steam offers no call to retrieve one game at the time.
            override fun shouldFetch(data: GameWithAchievements?) = false

            override fun loadFromDb(): LiveData<GameWithAchievements> = dao.getGamesWithAchievementsAsLiveData(appId)

            override fun createCall(): LiveData<ApiResponse<BaseGameResponse>> = AbsentLiveData.create()
        }.asLiveData()
    }

    fun update(item: Game) {
        dao.upsert(item)
    }

    fun update(item: GameWithAchievements) {
        item.game?.let { game -> dao.upsert(game) }
    }
}