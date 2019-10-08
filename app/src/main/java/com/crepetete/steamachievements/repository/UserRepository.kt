package com.crepetete.steamachievements.repository

import android.content.SharedPreferences
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.ApiSuccessResponse
import com.crepetete.steamachievements.db.dao.PlayerDao
import com.crepetete.steamachievements.repository.resource.LiveResource
import com.crepetete.steamachievements.repository.resource.NetworkBoundResource
import com.crepetete.steamachievements.vo.Player
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val api: SteamApiService,
    private val dao: PlayerDao
) : BaseRepository() {

    private companion object {
        private const val PREFS_USER_ID = "userId"
    }

    private val invalidUserId = "-1"

    /**
     * Fetches the [Player] that is currently logged in, or null if no user was found.
     */
    suspend fun getCurrentPlayer(): LiveResource<Player> {
        val playerId = getCurrentPlayerId(invalidUserId)

        return object : NetworkBoundResource<Player, Player?>() {
            override suspend fun saveCallResult(data: Player?) {
                data?.let { player ->
                    dao.insert(player)
                    putCurrentPlayerId(player.steamId)
                }
            }

            override fun shouldFetch(data: Player?) = data == null

            override suspend fun loadFromDb(): Player? {
                if (playerId != invalidUserId) {
                    return dao.getPlayerById(playerId)
                }
                return null
            }

            override suspend fun createCall(): Player? {
                if (playerId != invalidUserId) {
                    val result = api.getUserInfo(playerId)
                    if (result is ApiSuccessResponse) {
                        return result.body.response.players.firstOrNull()
                    }
                }
                return null
            }
        }.asLiveResource()
    }

    suspend fun getPlayer(playerId: String): LiveResource<Player> {
        return object : NetworkBoundResource<Player, Player?>() {
            override suspend fun saveCallResult(data: Player?) {
                data?.let { player ->
                    dao.insert(player)
                    putCurrentPlayerId(player.steamId)
                }
            }

            override fun shouldFetch(data: Player?) = data == null && playerId != invalidUserId

            override suspend fun loadFromDb(): Player? {
                return dao.getPlayerById(playerId)
            }

            override suspend fun createCall(): Player? {
                val response = api.getUserInfo(playerId)
                if (response is ApiSuccessResponse) {
                    return response.body.response.players.firstOrNull()
                }
                return null
            }
        }.asLiveResource()
    }

    // TODO move to preferencesRepository
    fun getCurrentPlayerId(defValue: String = "-1") = if (BuildConfig.DEBUG) {
        BuildConfig.TEST_USER_ID
    } else {
        sharedPreferences.getString(PREFS_USER_ID, defValue)!!
    }

    fun putCurrentPlayerId(userId: String) {
        sharedPreferences.edit().putString(PREFS_USER_ID, userId).apply()
    }
}