package com.crepetete.steamachievements.domain.usecases.player

import com.crepetete.steamachievements.domain.repository.UserRepository

/**
 * Retrieves the ID of the Player that is currently logged in.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 07 Jun, 2020; 14:02.
 */
class GetCurrentPlayerIdUseCaseImpl(
    private val userRepository: UserRepository
) : GetCurrentPlayerIdUseCase {
    override fun invoke(defValue: String?): String? {
        return userRepository.getCurrentPlayerId(defValue)
    }
}
