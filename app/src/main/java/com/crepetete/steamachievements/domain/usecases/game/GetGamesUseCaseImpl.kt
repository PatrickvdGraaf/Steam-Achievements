package com.crepetete.steamachievements.domain.usecases.game

import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.domain.repository.GameRepository
import com.crepetete.steamachievements.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow

/**
 * Retrieves a list of Games for the passed userId.
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 09 Jun, 2020; 22:35.
 */
class GetGamesUseCaseImpl(
    private val gameRepository: GameRepository,
    private val playerRepository: PlayerRepository
) : GetGamesUseCase {
    override fun invoke(userId: String?): Flow<Resource<BaseGameInfo>> {
        val id = userId ?: playerRepository.getCurrentPlayerId(Player.INVALID_ID)
        return gameRepository.getGames(id)
    }
}
