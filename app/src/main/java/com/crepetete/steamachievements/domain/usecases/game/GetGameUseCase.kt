package com.crepetete.steamachievements.domain.usecases.game

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.domain.model.Game

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Thu 11 Jun, 2020; 17:37.
 */
interface GetGameUseCase {
    operator fun invoke(appId: String): LiveData<Game>
}