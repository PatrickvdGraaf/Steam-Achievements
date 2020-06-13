package com.crepetete.steamachievements.domain.usecases.game

import com.crepetete.steamachievements.data.helper.LiveResource

/**
 * Retrieves a list of Games for the passed userId.
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 09 Jun, 2020; 22:32.
 */
interface UpdateGamesUseCase {
    operator fun invoke(userId: String? = null): LiveResource
}
