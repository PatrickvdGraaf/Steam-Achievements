package com.crepetete.steamachievements.data.repository.game

import android.arch.lifecycle.LiveData
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.model.Game
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(private val dao: GamesDao) {
    fun getGame(appId: String): LiveData<Game> = dao.getGameAsLiveData(appId)

    fun getGameFromDb(appId: String): Single<Game> = dao.getGame(appId)
}