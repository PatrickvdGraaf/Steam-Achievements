package com.crepetete.steamachievements.domain.repository

import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

/**
 * @author: Patrick van de Graaf.
 * @date: Wed 10 Jun, 2020; 14:38.
 */
interface AchievementsRepository {
    suspend fun updateAchievementsFromApi(userId: String, appId: String)
    suspend fun updateAchievementsFromApi(userId: String, appIds: List<String>)
    fun getAchievementsForGameAsFlow(appId: String): Flow<List<Achievement>>
    fun getAchievementsAsFlow(): Flow<Resource<Achievement>>
}