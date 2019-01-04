package com.crepetete.steamachievements.ui.fragment.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.util.AbsentLiveData
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.Resource
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    private var gameRepo: GameRepository,
    private var achievementsRepository: AchievementsRepository
) : ViewModel() {
    private val _userId = MutableLiveData<UserId>()
    val userId: LiveData<UserId>
        get() = _userId

    val games: LiveData<Resource<List<Game>>> = Transformations
        .switchMap(_userId) { id ->
            id.ifExists {
                gameRepo.getGames(it)
            }
        }

    // Create LiveData reference to the Achievements in the DB.
    var achievements: LiveData<List<Achievement>> = achievementsRepository.getAchievementsFromDb()

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

    fun updateAchievementsFor(appId: String, achievementsListener: AchievementsRepository.AchievementsListener) {
        achievementsRepository.loadAchievementsForGame(appId)
        achievementsRepository.updateAchievementsForGame(appId, achievementsListener)
        //        val achievementsForGame: LiveData<ApiResponse<SchemaResponse>> = achievementsRepository.getAchievementsFromApi(appId)
        //        val data = Transformations.switchMap(achievementsForGame) { response ->
        //            if (response is ApiSuccessResponse) {
        //                val value = Pair("", response.body.game.availableGameStats?.achievements)
        //                MutableLiveData<Pair<String, List<Achievement>>>()
        //            } else {
        //                AbsentLiveData.create<Pair<String, List<Achievement>>>()
        //            }
        //        }
    }

    fun updateAchievedStats(appId: String, achievements: List<Achievement>) {
        //        if (emptyAchievements.isNotEmpty()) {
        //            achievementsRepository.getAchievedStatusForAchievementsForGame(appId, emptyAchievements)
        //        }
    }

    fun updateGlobalStats(appId: String, achievements: List<Achievement>) {
        //        if (emptyAchievements.isNotEmpty()) {
        //            achievementsRepository.getGlobalAchievementStats(appId, emptyAchievements)
        //        }
    }

    fun setAppId(appId: String) {
        val update = UserId(appId)
        if (_userId.value == update) {
            return
        }
        _userId.value = update
    }

    fun updatePrimaryColorForGame(appId: String, rgb: Int) {
        gameRepo.getGameFromDb(appId).value?.let { game ->
            game.colorPrimaryDark = rgb
            gameRepo.update(game)
        }
    }

    data class UserId(val id: String) {
        fun <T> ifExists(f: (String) -> LiveData<T>): LiveData<T> {
            return if (id.isBlank()) {
                AbsentLiveData.create()
            } else {
                f(id)
            }
        }
    }
}