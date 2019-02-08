package com.crepetete.steamachievements.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameWithAchievements
import io.reactivex.Single

/**
 * Interface for database access on [Game] related operations.
 */
@Dao
@OpenForTesting
abstract class GamesDao : BaseDao<Game>() {

    @Query("SELECT * FROM games WHERE appId = :appId LIMIT 1")
    abstract fun getGame(appId: String): Single<Game>

    @Query("SELECT * FROM games WHERE appId = :appId LIMIT 1")
    abstract fun getGameAsLiveData(appId: String): LiveData<Game>

    @Transaction
    @Query("SELECT * FROM games WHERE appId IN (:ids)")
    abstract fun queryObjects(ids: List<String>): LiveData<List<GameWithAchievements>>

    @Query("SELECT * FROM games")
    abstract fun getGamesForUser(): Single<List<Game>>

    @Query("SELECT * FROM games")
    abstract fun getGamesAsLiveData(): LiveData<List<Game>?>

    @Query("SELECT appId FROM games")
    abstract fun getGameIds(): Single<List<String>>

    @Transaction
    @Query("SELECT * FROM games WHERE name LIKE :query")
    abstract fun search(query: String?): LiveData<List<GameWithAchievements>>

    @Transaction
    @Query("SELECT * FROM games")
    abstract fun getGamesWithAchievementsAsLiveData(): LiveData<List<GameWithAchievements>>

    @Transaction
    @Query("SELECT * FROM games WHERE appId = :appId LIMIT 1")
    abstract fun getGamesWithAchievementsAsLiveData(appId: String): LiveData<GameWithAchievements>

    @Delete
    abstract fun delete(vararg games: Game)
}