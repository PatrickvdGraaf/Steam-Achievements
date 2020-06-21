package com.crepetete.steamachievements.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.testing.OpenForTesting
import kotlinx.coroutines.flow.Flow

/**
 * Interface for database access on [Achievement] related operations.
 */
@Dao
@OpenForTesting
abstract class AchievementsDao : BaseDao<Achievement>() {
    @Query("SELECT * FROM achievements")
    abstract fun getAchievements(): List<Achievement>

    @Query("SELECT * FROM achievements WHERE appId=:appId")
    abstract fun getAchievements(appId: String): List<Achievement>

    @Query("SELECT * FROM achievements")
    abstract fun getAchievementsFlow(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE appId=:appId")
    abstract fun getAchievementsFlow(appId: String): Flow<List<Achievement>>
}