package com.crepetete.steamachievements.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.crepetete.data.helper.RateLimiter
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.helper.NetworkBoundResource
import com.crepetete.steamachievements.data.api.response.game.GamesResponse
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.domain.repository.GameRepository
import com.crepetete.steamachievements.domain.usecases.achievements.UpdateAchievementsUseCase
import com.crepetete.steamachievements.testing.OpenForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.Call
import java.util.concurrent.TimeUnit

@OpenForTesting
class GameRepositoryImpl(
    private val api: SteamApiService,
    private val gamesDao: GamesDao,
    private val updateAchievementsUseCase: UpdateAchievementsUseCase
) : GameRepository {

    private companion object {
        const val FETCH_GAMES_KEY = "FETCH_GAMES_KEY"
    }

    // Refresh games every day
    private val rateLimiter = RateLimiter<String>(1, TimeUnit.DAYS)

    /**
     * Fetch Games from both the Database and the API.
     * Refresh rate is set with the [rateLimiter].
     */
    override fun updateGames(userId: String?): LiveResource {
        CoroutineScope(Dispatchers.IO).launch {
            api.getGamesForUser(userId ?: Player.INVALID_ID).gamesResponse.games?.let { games ->
                val sortedGames = games.sortedByDescending { it.playTime }
                gamesDao.upsert(sortedGames)
                updateAchievementsUseCase(userId, sortedGames.map { it.appId.toString() })
            }
        }

        // TODO Find out why we cant create a Call<BaseGameResponse> and remove code above.
        return object : NetworkBoundResource<List<Game>, GamesResponse>() {
            override suspend fun createCall(): Call<GamesResponse> {
                return api.getGamesForUserAsCall(userId ?: Player.INVALID_ID)
//                return if (userId != null && rateLimiter.shouldFetch(FETCH_GAMES_KEY)) {
//                    api.getGamesForUser(userId).response.games
//                } else {
//                    null
//                }
            }

            override suspend fun saveCallResult(data: GamesResponse?) {
                data?.games?.let { games ->
                    gamesDao.upsert(games)

                    updateAchievementsUseCase(
                        userId,
                        games.sortedByDescending { it.playTime }.map { it.appId.toString() })
                }
            }
        }.asLiveResource()
    }

    override fun getGamesAsFlow(): Flow<List<BaseGameInfo>?> {
        return gamesDao.getGames()
    }

    /**
     * Fetch a specific [Game] form the Database.
     * Steam API doesn't provide a call for one specific game, so the data will only be the stored
     * value of the last API call.
     */
    override fun getGame(appId: String): LiveData<BaseGameInfo> {
        return gamesDao.getGame(appId).asLiveData()
    }

    override fun update(item: BaseGameInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            gamesDao.upsert(item)
        }
    }
}