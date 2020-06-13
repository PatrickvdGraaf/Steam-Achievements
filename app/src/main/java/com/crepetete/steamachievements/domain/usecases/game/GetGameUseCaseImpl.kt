package com.crepetete.steamachievements.domain.usecases.game

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.domain.repository.GameRepository

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Thu 11 Jun, 2020; 17:37.
 */
class GetGameUseCaseImpl(private val gameRepository: GameRepository) : GetGameUseCase {
    override fun invoke(appId: String): LiveData<Game> {
        return gameRepository.getGame(appId)
    }
}