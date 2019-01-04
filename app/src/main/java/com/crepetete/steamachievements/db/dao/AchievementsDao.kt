package com.crepetete.steamachievements.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.AchievementKeys

/**
 * Interface for database access on [Achievement] related operations.
 */
@Dao
@OpenForTesting
abstract class AchievementsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(list: List<Achievement>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(games: List<Achievement>)

    @Query("SELECT * FROM achievements")
    abstract fun getAchievements(): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE appId=:appId")
    abstract fun getAchievements(appId: String): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE appId=:appId AND name=:name")
    abstract fun getAchievements(appId: String, name: String?): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE name=:name AND appId=:appId")
    abstract fun getAchievement(name: String, appId: String): LiveData<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE appId=:appId")
    abstract fun getAchievementsForGame(appId: String): LiveData<List<Achievement>>

    @Query("SELECT name FROM achievements WHERE name = :name  AND appId = :appId LIMIT 1")
    abstract fun getAchievementKeys(name: String, appId: String): String?

    @Query("SELECT name, appId FROM achievements WHERE appId = :appId")
    abstract fun getAchievementKeys(appId: String): LiveData<List<AchievementKeys>>
}