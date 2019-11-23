package com.crepetete.steamachievements.repository.storage

import com.crepetete.steamachievements.vo.Player
import net.openid.appauth.AuthState

/**
 *
 * Layout for a class that supports requested functionality related to the apps Storage; the Shared
 * Preferences.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 10 Nov, 2019; 12:32.
 */
interface Storage {
    fun getPlayerId(defValue: String = Player.INVALID_ID): String
    fun setPlayerId(playerId: String)
    fun persistAuthState(authState: AuthState)
    fun getAuthState(): String?
    fun clearAuthState()
}