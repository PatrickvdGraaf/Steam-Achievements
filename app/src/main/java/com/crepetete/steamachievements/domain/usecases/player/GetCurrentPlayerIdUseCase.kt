package com.crepetete.steamachievements.domain.usecases.player

/**
 * Retrieves the ID of the Player that is currently logged in.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 07 Jun, 2020; 14:01.
 */
interface GetCurrentPlayerIdUseCase {
    operator fun invoke(defValue: String? = null): String?
}