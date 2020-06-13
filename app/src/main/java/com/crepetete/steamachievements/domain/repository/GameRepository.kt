package com.crepetete.steamachievements.domain.repository

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun updateGames(userId: String?): LiveResource
    fun getGamesAsFlow(): Flow<List<BaseGameInfo>?>
    fun getGame(appId: String): LiveData<Game>
    fun update(item: BaseGameInfo)
}