package com.crepetete.steamachievements.repository.storage

import com.crepetete.steamachievements.vo.Player

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 10 Nov, 2019; 12:32.
 */
interface Storage {
    fun getPlayerId(defValue: String = Player.INVALID_ID): String
    fun setPlayerId(playerId: String)
}