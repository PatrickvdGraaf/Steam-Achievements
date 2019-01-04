package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.ApiResponse
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.game.BaseGameResponse
import com.crepetete.steamachievements.db.dao.GamesDao
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.util.RateLimiter
import com.crepetete.steamachievements.vo.Game
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

    private val gameListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun getGames(userId: String): LiveData<Resource<List<Game>>> {
        return object : NetworkBoundResource<List<Game>, BaseGameResponse>(appExecutors) {
            override fun saveCallResult(item: BaseGameResponse) {
                Timber.d("Saving Games in DB")

                val games = item.response.games
                games.forEach { game ->
                    game.userId = userId
                }

                dao.insert(games)
            }

            override fun shouldFetch(data: List<Game>?) = data == null
                || data.isEmpty()
                || gameListRateLimit.shouldFetch("getGamesForUser$userId")

            override fun loadFromDb(): LiveData<List<Game>> = dao.getGamesAsLiveData()

            override fun createCall(): LiveData<ApiResponse<BaseGameResponse>> = api.getGamesForUser(userId)

            override fun onFetchFailed() {
                gameListRateLimit.reset(userId)
            }
        }.asLiveData()
    }

    /**
     * Inserts a single Game in the database.
     */
    fun insert(game: Game) {
        dao.insert(game)
    }

    /**
     * Inserts a list of Games into the database.
     */
    fun insert(games: List<Game>) {
        dao.insert(games)
    }

    /**
     * Returns a specific [Game] from the database.]
     *
     * @param appId ID of the requested Game.
     * @return LiveData object with the requested [Game]
     */
    fun getGameFromDb(appId: String): LiveData<Game> = dao.getGameAsLiveData(appId)

    fun update(item: Game) {
        appExecutors.diskIO().execute {
            dao.update(item)
        }
    }
}