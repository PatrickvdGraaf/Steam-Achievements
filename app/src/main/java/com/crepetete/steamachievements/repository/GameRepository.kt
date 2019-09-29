package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.db.dao.GamesDao
import com.crepetete.steamachievements.repository.limiter.RateLimiter
import com.crepetete.steamachievements.repository.resource.LiveResource
import com.crepetete.steamachievements.repository.resource.NetworkBoundResource
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.util.extensions.sort
import com.crepetete.steamachievements.vo.BaseGameInfo
import com.crepetete.steamachievements.vo.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class GameRepository @Inject constructor(
    private val dao: GamesDao,
    private val api: SteamApiService
) : BaseRepository() {

    // Refresh games every day
    private val gameListRateLimiter by lazy { RateLimiter<String>(1, TimeUnit.DAYS) }
    private val FETCH_GAMES_KEY = "FETCH_GAMES_KEY"

    /**
     * Fetch Games from both the Database and the API.
     * Refresh rate is set with the [gameListRateLimiter].
     */
    suspend fun getGames(userId: String, sortingType: SortingType): LiveResource<List<Game>> {
        return object : NetworkBoundResource<List<Game>, List<BaseGameInfo>>() {

            override suspend fun saveCallResult(data: List<BaseGameInfo>) {
                dao.insert(data)
            }

            override fun shouldFetch(data: List<Game>?): Boolean {
                return gameListRateLimiter.shouldFetch(FETCH_GAMES_KEY) || BuildConfig.DEBUG
            }

            override suspend fun createCall(): List<BaseGameInfo>? {
                val data = api.getGamesForUser(userId)
                return data.response.games
            }

            override suspend fun loadFromDb(): List<Game> {
                return dao.getGames().sort(sortingType)
            }
        }.asLiveResource()
    }

    /**
     * Fetch a specific [Game] form the Database.
     * Steam API doesn't provide a call for one specific game, so the data will only be the stored value of the last API call.
     */
    fun getGame(appId: String): LiveData<Game> {
        return liveData {
            emitSource(dao.getGame(appId))
        }
    }

    fun update(item: BaseGameInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            dao.upsert(item)
        }
    }
}