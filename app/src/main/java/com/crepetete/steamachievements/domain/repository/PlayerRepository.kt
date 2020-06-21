package com.crepetete.steamachievements.domain.repository

import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.model.Player

/**
 * @author: Patrick van de Graaf.
 * @date: Sun 07 Jun, 2020; 13:13.
 */
interface PlayerRepository {
    fun getPlayer(playerId: String): Resource
    fun getCurrentPlayerId(defValue: String = Player.INVALID_ID): String
    fun putCurrentPlayerId(playerId: String)
}