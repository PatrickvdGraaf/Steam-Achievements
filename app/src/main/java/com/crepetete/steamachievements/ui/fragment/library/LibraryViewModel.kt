package com.crepetete.steamachievements.ui.fragment.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.repository.UserRepository
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.crepetete.steamachievements.vo.Resource
import javax.inject.Inject

/**
 * ViewModel responsible for the [LibraryFragment].
 */
class LibraryViewModel @Inject constructor(
    private val gameRepo: GameRepository,
    private val achievementsRepository: AchievementsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private var sortingType = MutableLiveData<SortingType>()

    val mediatorLiveData = MediatorLiveData<Resource<List<GameWithAchievements>>>()
    private var gamesWithAchievement: LiveData<Resource<List<GameWithAchievements>>> = gameRepo.getGames(userRepository.getCurrentPlayerId())

    init {
        mediatorLiveData.addSource(gamesWithAchievement) { gamesResource ->
            mediatorLiveData.value = gamesResource
        }
        sortingType.value = SortingType.PLAYTIME
    }

    fun refresh() {
        mediatorLiveData.removeSource(gamesWithAchievement)
        gamesWithAchievement = gameRepo.getGames(userRepository.getCurrentPlayerId())
        mediatorLiveData.addSource(gamesWithAchievement) { gameResource ->
            mediatorLiveData.value = gameResource
        }
    }

    fun updateAchievements(appId: String,
                           listener: AchievementsRepository.AchievementsErrorListener) = achievementsRepository
        .getAchievements(appId, listener)

    fun updatePrimaryColorForGame(game: GameWithAchievements, rgb: Int) {
        game.setPrimaryColor(rgb)

        val gameData = game.game
        if (gameData != null) {
            gameRepo.update(gameData)
        }
    }
}