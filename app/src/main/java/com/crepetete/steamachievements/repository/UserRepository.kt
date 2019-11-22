package com.crepetete.steamachievements.repository

import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.ApiSuccessResponse
import com.crepetete.steamachievements.db.dao.PlayerDao
import com.crepetete.steamachievements.repository.resource.LiveResource
import com.crepetete.steamachievements.repository.resource.NetworkBoundResource
import com.crepetete.steamachievements.repository.storage.Storage
import com.crepetete.steamachievements.vo.Player
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val storage: Storage,
    private val api: SteamApiService,
    private val dao: PlayerDao
) {

    suspend fun getPlayer(playerId: String): LiveResource<Player> {
        return object : NetworkBoundResource<Player, Player?>() {
            override suspend fun saveCallResult(data: Player?) {
                data?.let { player ->
                    dao.insert(player)
                    putCurrentPlayerId(player.steamId)
                }
            }

            override fun shouldFetch(data: Player?) = data == null && playerId != Player.INVALID_ID

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

    fun getCurrentPlayerId() = storage.getPlayerId()

    fun putCurrentPlayerId(playerId: String) {
        storage.setPlayerId(playerId)
    }
}