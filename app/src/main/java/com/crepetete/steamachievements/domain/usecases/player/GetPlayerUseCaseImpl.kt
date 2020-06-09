package com.crepetete.steamachievements.domain.usecases.player

import com.crepetete.steamachievements.domain.repository.UserRepository

/**
 * Retrieves a player from the DB, and initiates a refresh.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 07 Jun, 2020; 13:44.
 */
class GetPlayerUseCaseImpl(private val userRepository: UserRepository) : GetPlayerUseCase {
    override fun invoke(id: String) = userRepository.getPlayer(id)
}