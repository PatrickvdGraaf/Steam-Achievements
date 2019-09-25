package com.crepetete.steamachievements.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.Player

/**
 * Interface for database access on [PlayerDao] related operations.
 */
@Dao
@OpenForTesting
abstract class PlayerDao : BaseDao<Player>() {
    @Insert(onConflict = REPLACE)
    abstract suspend fun insert(player: Player)

    @Query("SELECT * FROM players WHERE steamId = :id")
    abstract suspend fun getPlayerById(id: String): Player
}