package com.crepetete.steamachievements.repository

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.ApiResponse
import com.crepetete.steamachievements.api.response.user.UserResponse
import com.crepetete.steamachievements.db.dao.PlayerDao
import com.crepetete.steamachievements.repository.resource.NetworkBoundResource
import com.crepetete.steamachievements.util.AbsentLiveData
import com.crepetete.steamachievements.vo.Player
import com.crepetete.steamachievements.vo.Resource
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val sharedPreferences: SharedPreferences,
    private val api: SteamApiService,
    private val dao: PlayerDao
) {
    private val userIdKey = "userId"

    fun getCurrentPlayer(): LiveData<Resource<Player>> {
        val playerId = getCurrentPlayerId()

        if (playerId != null) {
            return object : NetworkBoundResource<Player, UserResponse>(appExecutors) {
                override fun saveCallResult(item: UserResponse) {
                    val user = item.response.players.firstOrNull()
                    if (user != null) {
                        dao.insert(user)
                        putCurrentPlayerId(user.steamId)
                    } else {
                        onFetchFailed()
                    }
                }

                override fun shouldFetch(data: Player?) = data == null

                override fun loadFromDb(): LiveData<Player> {
                    return dao.getPlayerById(playerId)
                }

                override fun createCall(): LiveData<ApiResponse<UserResponse>> {
                    return api.getUserInfo(playerId)
                }
            }.asLiveData()
        } else {
            return AbsentLiveData.create()
        }

    }

    fun getPlayer(playerId: String): LiveData<Resource<Player>> {
        return object : NetworkBoundResource<Player, UserResponse>(appExecutors) {
            override fun saveCallResult(item: UserResponse) {
                val user = item.response.players.firstOrNull()
                if (user != null) {
                    dao.insert(user)
                    putCurrentPlayerId(user.steamId)
                } else {
                    onFetchFailed()
                }
            }

            override fun shouldFetch(data: Player?) = data == null

            override fun loadFromDb(): LiveData<Player> {
                return dao.getPlayerById(playerId)
            }

            override fun createCall(): LiveData<ApiResponse<UserResponse>> {
                return api.getUserInfo(playerId)
            }
        }.asLiveData()
    }

    fun getCurrentPlayerId(): String? = sharedPreferences.getString(userIdKey, null)

    fun putCurrentPlayerId(userId: String) {
        sharedPreferences.edit().putString(userIdKey, userId).apply()
    }
}