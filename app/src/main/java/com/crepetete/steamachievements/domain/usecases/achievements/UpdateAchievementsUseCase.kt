package com.crepetete.steamachievements.domain.usecases.achievements

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Wed 10 Jun, 2020; 14:50.
 */
interface UpdateAchievementsUseCase {
    suspend operator fun invoke(userId: String? = null, appId: String)
    suspend operator fun invoke(userId: String? = null, appIds: List<String>)
}