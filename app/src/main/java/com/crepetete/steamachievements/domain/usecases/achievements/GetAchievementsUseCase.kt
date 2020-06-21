package com.crepetete.steamachievements.domain.usecases.achievements

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.domain.model.Achievement

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Sat 13 Jun, 2020; 18:55.
 */
interface GetAchievementsUseCase {
    operator fun invoke(appId: String): LiveData<List<Achievement>>
}