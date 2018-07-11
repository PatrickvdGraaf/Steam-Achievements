package com.crepetete.steamachievements.data.database.dao

import android.arch.persistence.room.*
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.AchievementKeys
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface AchievementsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(list: List<Achievement>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(games: List<Achievement>)

    @Query("SELECT * FROM achievements")
    fun getAchievements(): Single<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE appId=:appId AND name=:name")
    fun getAchievements(appId: String, name: String?): Single<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE appId=:appId")
    fun getAchievementsForGame(appId: String): Observable<List<Achievement>>

    @Query("SELECT name FROM achievements WHERE name = :name  AND appId = :appId LIMIT 1")
    fun getAchievementKeys(name: String, appId: String): String?

    @Query("SELECT name, appId FROM achievements WHERE appId = :appId")
    fun getAchievementKeys(appId: String): Single<List<AchievementKeys>>
}