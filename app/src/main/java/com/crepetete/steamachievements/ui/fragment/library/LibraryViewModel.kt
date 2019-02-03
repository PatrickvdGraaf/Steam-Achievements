package com.crepetete.steamachievements.ui.fragment.library

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.repository.UserRepository
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.crepetete.steamachievements.vo.Resource
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    private var gameRepo: GameRepository,
    private var achievementsRepository: AchievementsRepository,
    userRepository: UserRepository
) : ViewModel() {

    private var sortingType = MutableLiveData<SortingType>()

    val gamesWithAchievement: LiveData<Resource<List<GameWithAchievements>>> = gameRepo.getGames(userRepository.getCurrentPlayerId())

    init {
        sortingType.value = SortingType.PLAYTIME
    }

    fun updateAchievements(appId: String,
                           listener: AchievementsRepository.PrivateProfileMessageListener) = achievementsRepository.getAchievements(appId, listener)

    @SuppressLint("CheckResult")
    fun updatePrimaryColorForGame(game: GameWithAchievements, rgb: Int) {
        game.setPrimaryColor(rgb)

        val gameData = game.game
        if (gameData != null) {
            gameRepo.update(gameData)
        }
    }
}