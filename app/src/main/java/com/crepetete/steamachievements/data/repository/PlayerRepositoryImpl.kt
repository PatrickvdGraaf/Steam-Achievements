package com.crepetete.steamachievements.data.repository

import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.helper.NetworkBoundResource
import com.crepetete.steamachievements.data.api.response.user.UserResponse
import com.crepetete.steamachievements.data.database.dao.PlayerDao
import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.domain.repository.PlayerRepository
import com.crepetete.steamachievements.domain.repository.PreferencesRepository
import retrofit2.Call

class PlayerRepositoryImpl(
    private val storage: PreferencesRepository,
    private val api: SteamApiService,
    private val dao: PlayerDao
) : PlayerRepository {

    override fun getPlayer(playerId: String): LiveResource {
        return object : NetworkBoundResource<Player, UserResponse>() {
            override suspend fun createCall(): Call<UserResponse> {
                return api.getUserInfo(playerId)
            }

            override suspend fun saveCallResult(data: UserResponse?) {
                data?.response?.players?.get(0)?.let { player ->
                    dao.insert(player)
                    putCurrentPlayerId(player.steamId)
                }
            }
        }.asLiveResource()
    }

    override fun getCurrentPlayerId(defValue: String) =
        storage.getPlayerId(defValue) ?: defValue

    override fun putCurrentPlayerId(playerId: String) {
        storage.setPlayerId(playerId)
    }
}