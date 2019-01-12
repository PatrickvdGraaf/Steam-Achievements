package com.crepetete.steamachievements.ui.fragment.library

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.repository.GameRepository
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    private var gameRepo: GameRepository,
    private var achievementsRepository: AchievementsRepository
) : ViewModel() {

    val gamesWithAchievement= gameRepo.getGames()

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

    @SuppressLint("CheckResult")
    fun updatePrimaryColorForGame(appId: String, rgb: Int) {
        gameRepo.getGameFromDbAsSingle(appId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ game ->
                game.colorPrimaryDark = rgb
                gameRepo.update(game)
            }, { error ->
                Timber.e(error)
            })
    }
}