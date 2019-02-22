package com.crepetete.steamachievements.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.Player
import io.reactivex.Single

/**
 * Interface for database access on [PlayerDao] related operations.
 */
@Dao
@OpenForTesting
abstract class PlayerDao {
    @Insert(onConflict = REPLACE)
    abstract fun insert(player: Player)

    @Query("SELECT * FROM players WHERE steamId = :id")
    abstract fun getPlayerById(id: String): LiveData<Player>

    @Query("SELECT persona FROM players WHERE steamId = :id LIMIT 1")
    abstract fun getPlayerName(id: String): Single<String>
}