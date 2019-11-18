package com.crepetete.steamachievements.repository.storage

import android.content.Context
import javax.inject.Inject

/**
 * @author: Patrick van de Graaf.
 * @date: Sun 10 Nov, 2019; 12:31.
 */
class SharedPreferencesStorage @Inject constructor(context: Context) : Storage {

    private companion object {
        private const val PREFS_PLAYER_ID = "userId"
    }

    private val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    override fun getPlayerId(defValue: String): String {
        return sharedPreferences.getString(PREFS_PLAYER_ID, defValue)!!
    }

    override fun setPlayerId(playerId: String) {
        sharedPreferences.edit().putString(PREFS_PLAYER_ID, playerId).apply()
    }
}