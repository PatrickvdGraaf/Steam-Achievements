package com.crepetete.steamachievements.domain.usecases.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.domain.repository.AchievementsRepository
import com.crepetete.steamachievements.domain.repository.GameRepository
import com.crepetete.steamachievements.presentation.common.enums.SortingType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

/**
 * Creates a [Game] object by combining both the base game-info object Flow and the achievments Flow
 * from our Room database. This ensures that the LiveData object we return will be updated every
 * time that either of those two Flows detect changes in our DB.
 *
 * @author: Patrick van de Graaf.
 * @date: Wed 10 Jun, 2020; 15:39.
 */
class GetGamesFlowUseCaseImpl(
    private val gamesRepo: GameRepository,
    private val achievementsRepo: AchievementsRepository
) : GetGamesFlowUseCase {
    @ExperimentalCoroutinesApi
    override fun invoke(
        userId: String?,
        sortingTypeLiveData: LiveData<SortingType>?
    ): LiveData<List<Game>?> {
        val gamesFlow = gamesRepo.getGames(userId)
        val achievementsFlow = achievementsRepo.getAchievementsAsFlow()
        return gamesFlow.combine(achievementsFlow) { gamesBases, achievements ->
            if (gamesBases is Resource.Success<*>) {
                (gamesBases.data as? List<*>?)?.let { list ->

                }
            }
            gamesBases?.map { base ->
                Game(base, achievements.filter { it.appId == base.appId })
            }
        }
            .flowOn(Dispatchers.Default)
            .asLiveData()
    }
}