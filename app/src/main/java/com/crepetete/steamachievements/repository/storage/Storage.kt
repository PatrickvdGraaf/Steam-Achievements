package com.crepetete.steamachievements.repository.storage

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 10 Nov, 2019; 12:32.
 */
interface Storage {
    fun getPlayerId(defValue: String = "-1"): String
    fun setPlayerId(playerId: String)
}