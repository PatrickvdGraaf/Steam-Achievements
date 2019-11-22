package com.crepetete.steamachievements.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.Achievement

/**
 * Interface for database access on [Achievement] related operations.
 */
@Dao
@OpenForTesting
abstract class AchievementsDao : BaseDao<Achievement>() {
    @Query("SELECT * FROM achievements WHERE appId=:appId")
    abstract fun getAchievements(appId: String): List<Achievement>
}