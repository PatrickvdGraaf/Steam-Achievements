package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
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
    private val gameListRateLimit by lazy { RateLimiter<String>(1, TimeUnit.DAYS) }
    private val FETCH_GAMES_KEY = "FETCH_GAMES_KEY"

    suspend fun getGames(userId: String, sortingType: SortingType): LiveResource<List<Game>> {
        return object : NetworkBoundResource<List<Game>, List<BaseGameInfo>>() {

            override suspend fun saveCallResult(data: List<BaseGameInfo>) {
                dao.upsert(data)
            }

            override fun shouldFetch(data: List<Game>?): Boolean {
                return gameListRateLimit.shouldFetch(FETCH_GAMES_KEY)
            }

            override suspend fun createCall(): List<BaseGameInfo>? {
                val apiResponse = api.getGamesForUser(userId)
                return listOf()
            }

            override suspend fun loadFromDb(): List<Game> {
                return dao.getGames().sort(sortingType)
            }
        }.asLiveResource()
    }

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