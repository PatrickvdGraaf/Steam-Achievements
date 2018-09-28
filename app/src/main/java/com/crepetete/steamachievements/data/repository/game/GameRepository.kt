package com.crepetete.steamachievements.data.repository.game

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.response.ApiResponse
import com.crepetete.steamachievements.data.api.response.game.BaseGameResponse
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.database.model.GameWithAchievements
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.utils.RateLimiter
import com.crepetete.steamachievements.utils.resource.NetworkBoundResource
import com.crepetete.steamachievements.utils.resource.Resource
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
        private val appExecutors: AppExecutors,
        private val gamesDao: GamesDao,
        private val api: SteamApiService) {

    private val gameListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    /**
     * Returns a specific [Game] from the database.]
     *
     * @param appId ID of the requested Game.
     * @return LiveData object with the requested [Game]
     */
    fun getGameFromDb(appId: String): LiveData<Game> = gamesDao.getGameAsLiveData(appId)

    fun getGames(userId: String): LiveData<Resource<List<GameWithAchievements>>> {
        return object : NetworkBoundResource<List<GameWithAchievements>,
                BaseGameResponse>(appExecutors) {
            override fun saveCallResult(item: BaseGameResponse) {
                val games = item.response.games
                for (game in games) {
                    game.userId = userId
                }

                gamesDao.insert(games)
            }

            override fun shouldFetch(data: List<GameWithAchievements>?) = data == null
                    || data.isEmpty()
                    || gameListRateLimit.shouldFetch("getGames$userId")


            override fun loadFromDb(): LiveData<List<GameWithAchievements>> {
                return gamesDao.getGamesAsLiveData()
            }

            override fun createCall(): LiveData<ApiResponse<BaseGameResponse>> = api.getGamesForUserAsLiveData(userId)

            override fun onFetchFailed() {
                gameListRateLimit.reset(userId)
            }
        }.asLiveData()
    }

    fun update(item: Game) {
        appExecutors.diskIO().execute {
            gamesDao.update(item)
        }
    }
}