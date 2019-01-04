package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.ApiResponse
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.game.BaseGameResponse
import com.crepetete.steamachievements.db.dao.GamesDao
import com.crepetete.steamachievements.repository.user.UserRepository
import com.crepetete.steamachievements.util.RateLimiter
import com.crepetete.steamachievements.vo.Resource
import com.crepetete.steamachievements.vo.Game
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val dao: GamesDao,
    private val api: SteamApiService,
    private val userRepository: UserRepository
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

            override fun loadFromDb(): LiveData<List<Game>> {
                Timber.d("Getting games from DB.")
                val g = dao.getGamesAsLiveData()
                val gValue = g.value
                return g
            }

            override fun createCall(): LiveData<ApiResponse<BaseGameResponse>> = api.getGamesForUserAsLiveData(userId)

            override fun onFetchFailed() {
                gameListRateLimit.reset(userId)
            }
        }.asLiveData()
    }

//    /**
//     * Retrieves a list of all Game IDs in the database.
//     */
//    fun getGameIds(): Single<List<String>> {
//        return dao.getGameIds()
//    }
//
//    /**
//     * Retrieves a list of all Games in the database.
//     */
//    fun getGamesFromDb(): Single<List<Game>> {
//        return dao.getGamesForUser()
//    }

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

//    /**
//     * Retrieves all owned games from API.
//     * It also retrieves the Achievements for each games from the API before the list is returned.
//     * This is because Achievements are retrieved in a separate call.
//     */
//    fun getGamesFromApi(): Single<List<Game>> {
//        val userId = userRepository.getCurrentPlayerId()
//        return api.getGamesForUser(userId)
//            .map {
//                it.response.games
//            }
//            .doAfterSuccess {
//                insertOrUpdateGames(it, userId)
//            }
//    }

//    private fun insertOrUpdateGames(games: List<Game>, userId: String) {
//        getGameIds().subscribe({ ids ->
//            val newGames = games
//                .filter { !ids.contains(it.appId) }
//                .map {
//                    it.lastUpdated = Calendar.getInstance().time.time
//                    it
//                }
//            if (newGames.isNotEmpty()) {
//                newGames.map {
//                    it.userId = userId
//                }
//                newGames.forEach {
//                    insertGame(it)
//                }
//            }
//
//            val updatedGames = games
//                .filter {
//                    ids.contains(it.appId) && it.shouldUpdate()
//                }.map {
//                    it.lastUpdated = Calendar.getInstance().time.time
//                    it.userId = userId
//                    it
//                }
//            if (updatedGames.isNotEmpty()) {
//                updatedGames.forEach {
//                    updateGame(it)
//                }
//            }
//        }, {
//            Timber.e(it)
//        })
//    }

//    fun updateGame(game: Game) {
//        Single.fromCallable { dao.update(listOf(game)) }
//            .subscribeOn(Schedulers.computation())
//            .observeOn(Schedulers.io())
//            .subscribe({
//                Timber.d("Updated ${game.name} in the Database.")
//            }, {
//                Timber.e(it)
//            })
//    }
//
//    private fun insertGame(game: Game) {
//        Single.fromCallable { dao.insert(listOf(game)) }
//            .subscribeOn(Schedulers.computation())
//            .observeOn(Schedulers.io())
//            .subscribe({
//                Timber.d("Updated ${game.name} in the Database.")
//            }, {
//                Timber.e(it)
//            })
//    }

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