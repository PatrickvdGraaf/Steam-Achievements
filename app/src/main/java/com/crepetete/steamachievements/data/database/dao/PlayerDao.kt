package com.crepetete.steamachievements.data.database.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.crepetete.steamachievements.model.Player
import io.reactivex.Single

@Dao
interface PlayerDao {
    @Insert(onConflict = REPLACE)
    fun insert(player: Player)

    @Query("SELECT * FROM players WHERE steamId = :id")
    fun getPlayerById(id: String): Single<List<Player>>

    @Query("SELECT persona FROM players WHERE steamId = :id LIMIT 1")
    fun getPlayerName(id: String): Single<String>
}