package com.crepetete.steamachievements.data.repository.game

import android.arch.lifecycle.LiveData
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.model.Game
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(private val dao: GamesDao) {
    fun getGame(appId: String): LiveData<Game> {
        return dao.getGameAsLiveData(appId)
    }
}