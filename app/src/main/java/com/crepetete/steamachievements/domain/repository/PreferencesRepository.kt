package com.crepetete.steamachievements.domain.repository

/**
 *
 * Layout for a class that supports requested functionality related to the apps Storage; the Shared
 * Preferences.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 10 Nov, 2019; 12:32.
 */
interface PreferencesRepository {
    fun getPlayerId(defValue: String?): String?
    fun setPlayerId(playerId: String?)
}