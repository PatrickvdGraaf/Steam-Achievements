package com.crepetete.steamachievements.domain.usecases.player

import com.crepetete.steamachievements.domain.repository.PreferencesRepository

/**
 * Saves the ID for a newly logged in Player in the SharedPreferences.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 07 Jun, 2020; 14:08.
 */
class SaveCurrentPlayerIdUserCaseImpl(
    private val prefsRepository: PreferencesRepository
) : SaveCurrentPlayerIdUserCase {
    override fun invoke(newId: String?) {
        prefsRepository.setPlayerId(newId)
    }
}