package com.crepetete.steamachievements.domain.usecases.achievements

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.domain.repository.AchievementsRepository

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Sat 13 Jun, 2020; 18:56.
 */
class GetAchievementsUseCaseImpl(
    private val achievementsRepository: AchievementsRepository
) : GetAchievementsUseCase {
    override fun invoke(appId: String): LiveData<List<Achievement>> {
        return achievementsRepository.getAchievementsForGameAsFlow(appId).asLiveData()
    }
}