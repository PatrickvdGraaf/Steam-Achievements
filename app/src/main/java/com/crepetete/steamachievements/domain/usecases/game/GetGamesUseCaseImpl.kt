package com.crepetete.steamachievements.domain.usecases.game

import com.crepetete.data.helper.LiveResource
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.domain.repository.GameRepository
import com.crepetete.steamachievements.domain.repository.UserRepository

/**
 * Retrieves a list of Games for the passed userId.
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 09 Jun, 2020; 22:35.
 */
class GetGamesUseCaseImpl(
    private val gameRepository: GameRepository,
    private val userRepository: UserRepository
) : GetGamesUseCase {
    override fun invoke(userId: String?): LiveResource<List<Game>> {
        val id = userId ?: userRepository.getCurrentPlayerId(Player.INVALID_ID)
        return gameRepository.getGames(id)
    }
}
