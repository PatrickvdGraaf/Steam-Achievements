package com.crepetete.steamachievements.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameWithAchievements
import io.reactivex.Single

/**
 * Interface for database access on [Game] related operations.
 */
@Dao
@OpenForTesting
abstract class GamesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg games: Game)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(games: List<Game>)

    @Query("SELECT * FROM games WHERE appId = :appId LIMIT 1")
    abstract fun getGame(appId: String): Single<Game>

    @Query("SELECT * FROM games WHERE appId = :appId LIMIT 1")
    abstract fun getGameAsLiveData(appId: String): LiveData<Game>

    @Update(onConflict = REPLACE)
    abstract fun update(game: Game)

    @Update(onConflict = REPLACE)
    abstract fun update(games: List<Game>)

    @Delete
    abstract fun delete(vararg games: Game)

    @Query("SELECT * FROM games")
    abstract fun getGamesForUser(): Single<List<Game>>

    @Transaction
    @Query("SELECT * FROM games")
    abstract fun getGamesAsLiveData(): LiveData<List<Game>>

    @Query("SELECT appId FROM games")
    abstract fun getGameIds(): Single<List<String>>

    @Query("SELECT * FROM games")
    abstract fun getGamesWithAchievementsAsLiveData(): LiveData<List<GameWithAchievements>>

    @Query("SELECT * FROM games WHERE appId = :appId LIMIT 1")
    abstract fun getGamesWithAchievementsAsLiveData(appId: String): LiveData<GameWithAchievements>
}