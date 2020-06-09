package com.crepetete.steamachievements.domain.usecases.player

/**
 * Saves the ID for a newly logged in Player in the SharedPreferences.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 07 Jun, 2020; 14:07.
 */
interface SaveCurrentPlayerIdUserCase {
    operator fun invoke(newId: String?)
}