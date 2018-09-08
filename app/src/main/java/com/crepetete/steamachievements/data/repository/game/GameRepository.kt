package com.crepetete.steamachievements.data.repository.game

import android.arch.lifecycle.LiveData
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.utils.RateLimiter
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
        private val dao: GamesDao) {

    private val gameListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    /**
     * Returns a specific [Game] from the database.]
     *
     * @param appId ID of the requested Game.
     * @return LiveData object with the requested [Game]
     */
    fun getGameFromDb(appId: String): LiveData<Game> = dao.getGameAsLiveData(appId)

//    fun getGames(userId: String): LiveData<Resource<List<Game>>> {
//        return object : NetworkBoundResource<List<Game>, BaseGameResponse>(appExecutors) {
//            override fun saveCallResult(item: BaseGameResponse) {
//                dao.insert(item.response.games)
//            }
//
//            override fun shouldFetch(data: List<Game>?): Boolean {
//                Timber.i(data?.toString() ?: "No data")
//                return data == null || data.isEmpty()
//                        || gameListRateLimit.shouldFetch(userId)
//            }
//
//            override fun loadFromDb(): LiveData<List<Game>> = dao.getGamesAsLiveData()
//
//            override fun createCall(): LiveData<ApiResponse<BaseGameResponse>> = api.getGamesForUser(userId)
//
//            override fun onFetchFailed() {
//                gameListRateLimit.reset(userId)
//            }
//
//            override fun processResponse(response: ApiSuccessResponse<BaseGameResponse>)
//                    : BaseGameResponse {
//                if (BuildConfig.DEBUG) {
//                    Timber.i(response.body.toString())
//                }
//                return super.processResponse(response)
//            }
//        }.asLiveData()
//    }
}