package com.crepetete.steamachievements.data.repository.achievement

import com.crepetete.steamachievements.model.Achievement
import io.reactivex.Single

interface AchievementRepository {
    // Database
    fun insertAchievementsIntoDb(achievements: List<Achievement>, appId: String)

    fun getAllAchievements(): Single<List<Achievement>>

    fun getAchievementsFromApi(appIds: List<String>): Single<List<Achievement>>

    // API
    fun updateAchievementIntoDb(achievement: List<Achievement>)

    fun getAchievedStatusForAchievementsForGame(appId: String, allAchievements: List<Achievement>): Single<List<Achievement>>
    fun getAchievementsFromDb(appIds: List<String>): Single<List<Achievement>>
    fun getAchievementsFromDb(appId: String): Single<List<Achievement>>
}