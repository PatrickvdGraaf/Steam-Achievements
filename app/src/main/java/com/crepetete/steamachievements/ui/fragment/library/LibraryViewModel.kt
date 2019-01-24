package com.crepetete.steamachievements.ui.fragment.library

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.crepetete.steamachievements.vo.Resource
import java.util.*
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    private var gameRepo: GameRepository,
    private var achievementsRepository: AchievementsRepository
) : ViewModel() {

    private val _query = MutableLiveData<String>().apply {
        value = ""
    }
    val query: LiveData<String> = _query

    val gamesWithAchievement: LiveData<Resource<List<GameWithAchievements>>> = Transformations
        .switchMap(_query) { search ->
            if (search.isNullOrBlank()) {
                gameRepo.getGames()
            } else {
                gameRepo.search(search, SortingType.NAME)
            }
        }

    //    private var sortingType = SortingType.PLAYTIME

    //    fun rearrangeGames(order: SortingType) = games.value?.data?.map { it.game }?.let {
    //        val nonNullGames = mutableListOf<Game>()
    //        it.forEach { game ->
    //            if (game != null) {
    //                nonNullGames.add(game)
    //            }
    //        }
    //        games.value = nonNullGames.toList().sort(order)
    //    }.also { sortingType = order }

    // TODO find out why the loadAchievementsForGame method doesn't call API.
    fun updateAchievementsFor(appId: String) {
        // This doesn't work for API calls for some reason
        achievementsRepository.loadAchievementsForGame(appId)

        // This does work, but not as nice as I'd like.
        achievementsRepository.updateAchievementsForGame(appId)
    }

    fun setQuery(originalInput: String) {
        val input = originalInput.toLowerCase(Locale.getDefault()).trim()
        if (input == _query.value) {
            return
        }
        _query.value = input
    }

    @SuppressLint("CheckResult")
    fun updatePrimaryColorForGame(game: GameWithAchievements, rgb: Int) {
        game.setPrimaryColor(rgb)

        val gameData = game.game
        if (gameData != null) {
            gameRepo.update(gameData)
        }
    }
}