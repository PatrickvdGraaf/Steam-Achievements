package com.crepetete.steamachievements.domain.usecases.game

import com.crepetete.data.helper.LiveResource
import com.crepetete.steamachievements.domain.model.Game

/**
 * Retrieves a list of Games for the passed userId.
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 09 Jun, 2020; 22:32.
 */
interface GetGamesUseCase {
    operator fun invoke(userId: String? = null): LiveResource<List<Game>>
}
