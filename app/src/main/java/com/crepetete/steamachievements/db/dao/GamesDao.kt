package com.crepetete.steamachievements.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.BaseGameInfo
import com.crepetete.steamachievements.vo.Game

/**
 * Interface for database access on [BaseGameInfo] related operations.
 */
@Dao
@OpenForTesting
abstract class GamesDao : BaseDao<BaseGameInfo>() {

    @Transaction
    @Query("SELECT * FROM games WHERE name LIKE :query")
    abstract fun search(query: String?): LiveData<List<Game>>

    @Transaction
    @Query("SELECT * FROM games WHERE appId = :appId LIMIT 1")
    abstract fun getGame(appId: String): LiveData<Game>

    @Transaction
    @Query("SELECT * FROM games")
    abstract suspend fun getGames(): List<Game>

    @Query("SELECT * FROM games")
    abstract suspend fun getGamesInfo(): List<BaseGameInfo>

    @Delete
    abstract fun delete(vararg games: BaseGameInfo)
}