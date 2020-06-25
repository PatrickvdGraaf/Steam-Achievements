package com.crepetete.steamachievements.domain.usecases.news

import com.crepetete.steamachievements.data.helper.LiveResource

/**
 * Retrieves a list of NewsItems for the given game ID.
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 09 Jun, 2020; 23:22.
 */
interface UpdateNewsUseCase {
    operator fun invoke(gameId: String): LiveResource
}