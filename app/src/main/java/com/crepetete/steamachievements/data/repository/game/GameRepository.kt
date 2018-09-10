package com.crepetete.steamachievements.data.repository.game

import android.arch.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.response.ApiResponse
import com.crepetete.steamachievements.data.api.response.game.BaseGameResponse
import com.crepetete.steamachievements.data.database.dao.GamesDao
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
        private val dao: GamesDao,
        private val api: SteamApiService) {

    private val gameListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    /**
     * Returns a specific [Game] from the database.]
     *
     * @param appId ID of the requested Game.
     * @return LiveData object with the requested [Game]
     */
    fun getGameFromDb(appId: String): LiveData<Game> = dao.getGameAsLiveData(appId)

    fun getGames(userId: String): LiveData<Resource<List<Game>>> {
        return object : NetworkBoundResource<List<Game>, BaseGameResponse>(appExecutors) {
            override fun saveCallResult(item: BaseGameResponse) {
                val games = item.response.games
                for (game in games) {
                    game.userId = userId
                    if (!game.achievementsWereAdded()) {
                        game.setAchievements(listOf())
                    }
                }

                dao.insert(games)
            }

            override fun shouldFetch(data: List<Game>?) = data == null
                    || data.isEmpty()
                    || gameListRateLimit.shouldFetch(userId)


            override fun loadFromDb(): LiveData<List<Game>> {
                return dao.getGamesAsLiveData()
            }

            override fun createCall(): LiveData<ApiResponse<BaseGameResponse>> = api.getGamesForUserAsLiveData(userId)

            override fun onFetchFailed() {
                gameListRateLimit.reset(userId)
            }
        }.asLiveData()
    }
}