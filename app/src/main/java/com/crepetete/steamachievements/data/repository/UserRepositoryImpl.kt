package com.crepetete.steamachievements.data.repository

import com.crepetete.data.helper.LiveResource
import com.crepetete.data.helper.NetworkBoundResource
import com.crepetete.data.network.SteamApiService
import com.crepetete.data.network.response.base.ApiSuccessResponse
import com.crepetete.steamachievements.data.database.dao.PlayerDao
import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.domain.repository.PreferencesRepository
import com.crepetete.steamachievements.domain.repository.UserRepository

class UserRepositoryImpl(
    private val storage: PreferencesRepository,
    private val api: SteamApiService,
    private val dao: PlayerDao
) : UserRepository {

    override fun getPlayer(playerId: String): LiveResource<Player> {
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

    override fun getCurrentPlayerId(defValue: String?) = storage.getPlayerId(defValue)

    override fun putCurrentPlayerId(playerId: String) {
        storage.setPlayerId(playerId)
    }
}