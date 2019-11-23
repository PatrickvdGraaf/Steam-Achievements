package com.crepetete.steamachievements.repository.storage

import android.content.Context
import net.openid.appauth.AuthState
import javax.inject.Inject

/**
 * @author: Patrick van de Graaf.
 * @date: Sun 10 Nov, 2019; 12:31.
 */
class SharedPreferencesStorage @Inject constructor(context: Context) : Storage {

    private companion object {
        private const val SHARED_PREFERENCES_NAME = "USER_PREFS"
        private const val AUTH_STATE = "AUTH_STATE"
        private const val PREFS_PLAYER_ID = "PREFS_PLAYER_ID"
    }

    private val sharedPreferences = context.getSharedPreferences(
        SHARED_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun getPlayerId(defValue: String): String {
        return sharedPreferences.getString(PREFS_PLAYER_ID, defValue)!!
    }

    override fun setPlayerId(playerId: String) {
        sharedPreferences.edit().putString(PREFS_PLAYER_ID, playerId).apply()
    }

    override fun persistAuthState(authState: AuthState) {
        sharedPreferences.edit()
            .putString(AUTH_STATE, authState.toJsonString())
            .apply()
    }

    override fun getAuthState(): String? {
        return sharedPreferences.getString(AUTH_STATE, null)
    }

    override fun clearAuthState() {
        sharedPreferences.edit()
            .remove(AUTH_STATE)
            .apply()
    }
}