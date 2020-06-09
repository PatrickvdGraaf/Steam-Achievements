package com.crepetete.steamachievements.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.testing.OpenForTesting

/**
 * Interface for database access on [BaseGameInfo] related operations.
 */
@Dao
@OpenForTesting
abstract class GamesDao : BaseDao<BaseGameInfo>() {
    @Transaction
    @Query("SELECT * FROM games WHERE appId = :appId LIMIT 1")
    abstract fun getGame(appId: String): LiveData<Game>

    @Transaction
    @Query("SELECT * FROM games")
    abstract suspend fun getGames(): List<Game>
}