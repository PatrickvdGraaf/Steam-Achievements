package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.ApiResponse
import com.crepetete.steamachievements.api.response.game.BaseGameResponse
import com.crepetete.steamachievements.db.dao.GamesDao
import com.crepetete.steamachievements.repository.limiter.RateLimiter
import com.crepetete.steamachievements.repository.resource.NetworkBoundResource
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.util.extensions.sort
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
    private val api: SteamApiService,
    private val userRepository: UserRepository
) {

    // Refresh games every day
    private val gameListRateLimit = RateLimiter<String>(1, TimeUnit.DAYS)

    fun getGames(userId: String = userRepository.getCurrentPlayerId() ?: ""): LiveData<Resource<List<GameWithAchievements>>> {
        return object : NetworkBoundResource<List<GameWithAchievements>, BaseGameResponse>(appExecutors) {

            override fun saveCallResult(item: BaseGameResponse) {
                Timber.d("Saving Games in DB")

                val games = item.response.games
                games.forEach { game ->
                    game.userId = userId
                }

                dao.upsert(games)
            }

            override fun shouldFetch(data: List<GameWithAchievements>?) = data == null
                || data.isEmpty()
                || gameListRateLimit.shouldFetch("getGamesForUser$userId")

            override fun loadFromDb(): LiveData<List<GameWithAchievements>> = dao.getGamesWithAchievementsAsLiveData()

            override fun createCall(): LiveData<ApiResponse<BaseGameResponse>> = api.getGamesForUser(userId)

            override fun onFetchFailed() {
                gameListRateLimit.reset(userId)
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
        dao.update(item)
    }

    fun update(item: GameWithAchievements) {
        val game = item.game
        if (game != null) {
            dao.update(game)
        }
    }

    fun search(query: String?, sortingType: SortingType): LiveData<Resource<List<GameWithAchievements>>> {
        return object : NetworkBoundResource<List<GameWithAchievements>, BaseGameResponse>(appExecutors) {

            override fun saveCallResult(item: BaseGameResponse) {
                val games = item.response.games
                games.forEach { game ->
                    game.userId = userRepository.getCurrentPlayerId() ?: ""
                }

                dao.upsert(games)
            }

            // Only search in database.
            override fun shouldFetch(data: List<GameWithAchievements>?) = false

            override fun loadFromDb(): LiveData<List<GameWithAchievements>> = Transformations.map(dao.search(query)) { games ->
                games?.sort(sortingType)
                games ?: listOf()
            }

            override fun createCall(): LiveData<ApiResponse<BaseGameResponse>> = AbsentLiveData.create()
        }.asLiveData()
    }
}