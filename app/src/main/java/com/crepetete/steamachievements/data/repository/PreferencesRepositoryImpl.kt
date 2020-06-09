package com.crepetete.steamachievements.data.repository

import android.content.Context
import com.crepetete.steamachievements.domain.repository.PreferencesRepository
import javax.inject.Inject

/**
 * @author: Patrick van de Graaf.
 * @date: Sun 10 Nov, 2019; 12:31.
 */
class PreferencesRepositoryImpl @Inject constructor(context: Context) : PreferencesRepository {

    private companion object {
        private const val PREFS_NAME = "STEAM_ACHIEVEMENTS_PREFS_NAME"
        private const val PREFS_KEY_PLAYER_ID = "PREFS_KEY_PLAYER_ID"
    }

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Retrieves the ID of the last logged in player.
     */
    override fun getPlayerId(defValue: String?): String? {
        return sharedPreferences.getString(PREFS_KEY_PLAYER_ID, defValue)
    }

    /**
     * Saves the ID of the player that is currently logged in.
     */
    override fun setPlayerId(playerId: String?) {
        sharedPreferences.edit().putString(PREFS_KEY_PLAYER_ID, playerId).apply()
    }
}