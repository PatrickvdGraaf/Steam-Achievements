package com.crepetete.steamachievements.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.AchievementKeys

/**
 * Interface for database access on [Achievement] related operations.
 */
@Dao
@OpenForTesting
abstract class AchievementsDao : BaseDao<Achievement>() {
    @Query("SELECT * FROM achievements")
    abstract fun getAchievements(): LiveData<List<Achievement>?>

    @Query("SELECT * FROM achievements WHERE appId=:appId")
    abstract fun getAchievements(appId: String): List<Achievement>

    @Query("SELECT * FROM achievements WHERE appId=:appId AND name=:name LIMIT 1")
    abstract fun getAchievements(appId: String, name: String?): LiveData<Achievement>

    @Query("SELECT * FROM achievements WHERE appId=:appId")
    abstract fun getAchievementsForGame(appId: String): LiveData<List<Achievement>>

    @Query("SELECT name FROM achievements WHERE name = :name AND appId = :appId LIMIT 1")
    abstract fun getAchievementKeys(name: String, appId: String): String?

    @Query("SELECT name, appId FROM achievements WHERE appId = :appId")
    abstract fun getAchievementKeys(appId: String): LiveData<List<AchievementKeys>>
}