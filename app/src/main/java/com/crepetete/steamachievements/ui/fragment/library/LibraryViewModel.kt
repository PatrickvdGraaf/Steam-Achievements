package com.crepetete.steamachievements.ui.fragment.library

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.util.AbsentLiveData
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.crepetete.steamachievements.vo.Resource
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    private var gameRepo: GameRepository,
    private var achievementsRepository: AchievementsRepository
) : ViewModel() {
    private val _userId = MutableLiveData<UserId>()
    private val userId: LiveData<UserId>
        get() = _userId

    val games: LiveData<Resource<List<Game>>> = Transformations
        .switchMap(userId) { id ->
            id.ifExists {
                gameRepo.getGames(it)
            }
        }

    val gamesWithAchievement: LiveData<List<GameWithAchievements>> = gameRepo.getGamesWithAchievements()

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

    fun setAppId(appId: String) {
        val update = UserId(appId)
        if (_userId.value == update) {
            return
        }
        _userId.value = update
    }

    @SuppressLint("CheckResult")
    fun updatePrimaryColorForGame(appId: String, rgb: Int) {
        gameRepo.getGameFromDbAsSingle(appId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({game ->
                game.colorPrimaryDark = rgb
                gameRepo.update(game)
            }, {error ->
                Timber.e(error)
            })
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