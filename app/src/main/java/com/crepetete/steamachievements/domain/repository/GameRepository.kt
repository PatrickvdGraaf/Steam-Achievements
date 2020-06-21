package com.crepetete.steamachievements.domain.repository

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getGames(userId: String?): Flow<Resource<List<BaseGameInfo>>>
    fun getGamesAsFlow(): Flow<List<BaseGameInfo>?>
    fun getGame(appId: String): LiveData<BaseGameInfo>
    fun update(item: BaseGameInfo)
}