package com.crepetete.steamachievements.domain.usecases.player

import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.domain.model.Player

/**
 * @author: Patrick van de Graaf.
 * @date: Sun 07 Jun, 2020; 13:43.
 */
interface GetPlayerUseCase {
    operator fun invoke(id: String): LiveResource<Player>
}