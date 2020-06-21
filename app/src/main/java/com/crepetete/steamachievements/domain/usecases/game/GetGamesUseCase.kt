package com.crepetete.steamachievements.domain.usecases.game

import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import kotlinx.coroutines.flow.Flow

/**
 * Retrieves a list of Games for the passed userId.
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 09 Jun, 2020; 22:32.
 */
interface GetGamesUseCase {
    operator fun invoke(userId: String? = null): Flow<Resource<BaseGameInfo>>
}
