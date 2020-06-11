package com.crepetete.steamachievements.domain.usecases.game

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.presentation.common.enums.SortingType

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Wed 10 Jun, 2020; 15:39.
 */
interface GetGamesFlowUseCase {
    operator fun invoke(
        userId: String? = null,
        sortingTypeLiveData: LiveData<SortingType>? = null
    ): LiveData<List<Game>?>
}