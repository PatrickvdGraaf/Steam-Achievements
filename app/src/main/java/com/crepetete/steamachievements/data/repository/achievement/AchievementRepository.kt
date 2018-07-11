package com.crepetete.steamachievements.data.repository.achievement

import com.crepetete.steamachievements.model.Achievement
import io.reactivex.Observable
import io.reactivex.Single

interface AchievementRepository {
    // General
    fun getAchievements(appId: String): Observable<List<Achievement>>

    // Database
    fun insertAchievementsIntoDb(achievements: List<Achievement>, appId: String)

    fun getAllAchievements(): Single<List<Achievement>>

    fun getAchievementsFromApi(appIds: List<String>): Observable<List<Achievement>>

    // API
    fun updateAchievementIntoDb(achievement: List<Achievement>)

    fun getPlayerAchievementsForGame(appId: String, allAchievements: List<Achievement>): Single<List<Achievement>>
}