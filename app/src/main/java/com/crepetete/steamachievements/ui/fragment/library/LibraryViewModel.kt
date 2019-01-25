package com.crepetete.steamachievements.ui.fragment.library

import android.annotation.SuppressLint
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

class LibraryViewModel @Inject constructor(
    private var gameRepo: GameRepository,
    private var achievementsRepository: AchievementsRepository,
    userRepository: UserRepository
) : ViewModel() {

    private var sortingType = MutableLiveData<SortingType>()

    val gamesWithAchievement: LiveData<Resource<List<GameWithAchievements>>> = gameRepo.getGames(userRepository.getCurrentPlayerId() ?: "")

    //    fun rearrangeGames(order: SortingType) = gamesWithAchievement.value?.data?.map { it.game }?.let {
    //        val nonNullGames = mutableListOf<Game>()
    //        it.forEach { game ->
    //            if (game != null) {
    //                nonNullGames.add(game)
    //            }
    //        }
    //        gamesWithAchievement.value = nonNullGames.toList().sort(order)
    //    }.also { sortingType = order }

    init {
        sortingType.value = SortingType.PLAYTIME
    }

    // TODO find out why the loadAchievementsForGame method doesn't call API.
    fun updateAchievementsFor(appId: String) {
        // This doesn't work for API calls for some reason
        achievementsRepository.loadAchievementsForGame(appId)

        // This does work, but not as nice as I'd like.
        achievementsRepository.updateAchievementsForGame(appId)
    }

    @SuppressLint("CheckResult")
    fun updatePrimaryColorForGame(game: GameWithAchievements, rgb: Int) {
        game.setPrimaryColor(rgb)

        val gameData = game.game
        if (gameData != null) {
            gameRepo.update(gameData)
        }
    }

    internal class DoubleLiveData(
        firstLiveData: LiveData<String?>,
        secondLiveData: LiveData<SortingType>) : MediatorLiveData<Pair<String, SortingType>>() {

        init {
            addSource(firstLiveData) { first -> value = Pair(first ?: "", secondLiveData.value ?: SortingType.PLAYTIME) }
            addSource(secondLiveData) { second -> value = Pair(firstLiveData.value ?: "", second) }
        }
    }
}