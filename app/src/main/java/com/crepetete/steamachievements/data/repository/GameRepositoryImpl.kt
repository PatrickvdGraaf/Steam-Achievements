package com.crepetete.steamachievements.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.crepetete.data.helper.RateLimiter
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.helper.NetworkBoundResource
import com.crepetete.steamachievements.data.api.response.game.GamesResponse
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.domain.repository.GameRepository
import com.crepetete.steamachievements.domain.repository.PreferencesRepository
import com.crepetete.steamachievements.domain.usecases.achievements.UpdateAchievementsUseCase
import com.crepetete.steamachievements.testing.OpenForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import retrofit2.Call
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

@OpenForTesting
class GameRepositoryImpl(
    private val api: SteamApiService,
    private val gamesDao: GamesDao,
    private val updateAchievementsUseCase: UpdateAchievementsUseCase,
    private val sharedPreferencesRepository: PreferencesRepository
) : GameRepository {

    private companion object {
        const val MAX_REFRESH_MILLIS = 1000 * 60 * 15
    }

    // Refresh games every day
    private val rateLimiter = RateLimiter<String>(1, TimeUnit.DAYS)

    /**
     * Fetch Games from both the Database and the API.
     * Refresh rate is set with the [rateLimiter].
     */
    @FlowPreview
    override fun getGames(userId: String?): Flow<Resource<List<BaseGameInfo>>> {
        return object : NetworkBoundResource<List<BaseGameInfo>, GamesResponse>() {

            override suspend fun loadFromDb(): List<BaseGameInfo> {
                return userId?.let {
                    gamesDao.getGames(userId)
                } ?: listOf()
            }

            override suspend fun fetchFromNetwork(): Call<GamesResponse> {
                return api.getGamesForUserAsCall(userId ?: Player.INVALID_ID)
            }

            override suspend fun shouldFetch(
                data: List<BaseGameInfo>?, dataFetchDate: Date?
            ): Boolean {
                return data == null
                        || dataFetchDate == null
                        || (dataFetchDate.time - Calendar.getInstance().time.time) > MAX_REFRESH_MILLIS
            }

            override fun convertApiResult(result: GamesResponse?) = result?.games

            override suspend fun saveCallResult(data: GamesResponse?) {
                // We only save games that the player owns.
                if (userId == sharedPreferencesRepository.getPlayerId(null))
                    data?.games?.let { games ->
                        gamesDao.upsert(games)

                        updateAchievementsUseCase(
                            userId,
                            games.sortedByDescending { it.playTime }.map { it.appId.toString() })
                    }
            }

            override suspend fun getDataFetchDate(data: List<BaseGameInfo>?): Date? {
                // TODO add proper fetch limits
                return Calendar.getInstance().time
            }

        }.asFlow()
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