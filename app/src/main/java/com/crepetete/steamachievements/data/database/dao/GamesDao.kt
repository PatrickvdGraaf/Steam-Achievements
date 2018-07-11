package com.crepetete.steamachievements.data.database.dao

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.IGNORE
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.crepetete.steamachievements.model.Game
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface GamesDao {
    @Insert(onConflict = IGNORE)
    fun insert(games: List<Game>)

    @Query("SELECT * FROM games WHERE appId = :appId")
    fun getGame(appId: String): Single<List<Game>>

    @Update(onConflict = REPLACE)
    fun update(games: List<Game>)

    @Delete
    fun delete(vararg games: Game)

    @Query("SELECT * FROM games")
    fun getGamesForUser(): Observable<List<Game>>

    @Query("SELECT appId FROM games")
    fun getGameIds(): Single<List<String>>
}