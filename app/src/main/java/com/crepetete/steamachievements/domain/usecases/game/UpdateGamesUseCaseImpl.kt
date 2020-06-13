package com.crepetete.steamachievements.domain.usecases.game

import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.domain.repository.GameRepository
import com.crepetete.steamachievements.domain.repository.PlayerRepository

/**
 * Retrieves a list of Games for the passed userId.
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 09 Jun, 2020; 22:35.
 */
class UpdateGamesUseCaseImpl(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository
) : UpdateGamesUseCase {
    override fun invoke(userId: String?): LiveResource {
        val id = userId ?: playerRepository.getCurrentPlayerId(Player.INVALID_ID)
        return gameRepository.updateGames(id)
    }
}
