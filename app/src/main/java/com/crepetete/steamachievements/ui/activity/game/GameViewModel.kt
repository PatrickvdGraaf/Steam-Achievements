package com.crepetete.steamachievements.ui.activity.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.palette.graphics.Palette
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.ui.common.adapter.sorting.AchievementSortedListImpl
import com.crepetete.steamachievements.ui.common.enums.Order
import com.crepetete.steamachievements.util.livedata.AbsentLiveData
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.crepetete.steamachievements.vo.Resource
import javax.inject.Inject

class GameViewModel @Inject constructor(
    private val gameRepo: GameRepository
) : ViewModel() {

    private val _appId = MutableLiveData<AppId>()
    val appId: LiveData<AppId>
        get() = _appId

    val game: LiveData<Resource<GameWithAchievements>> = Transformations
        .switchMap(_appId) { id ->
            id.ifExists { appId ->
                gameRepo.getGame(appId)
            }
        }

    private val sortingComparator = MutableLiveData<Order.BaseComparator<Achievement>>()

    private var index = 0

    private val sortingMethods: HashMap<Int, Order.BaseComparator<Achievement>> = hashMapOf(
        0 to Order.AchievedOrder(),
        1 to Order.RarityOrder(),
        2 to Order.NotAchievedOrder())

    /* Colors */
    val vibrantColor: MutableLiveData<Palette.Swatch> = MutableLiveData()
    val mutedColor: MutableLiveData<Palette.Swatch> = MutableLiveData()

    init {
        setAchievementSortingMethod(AchievementSortedListImpl.DEFAULT_ORDER)
    }
    /**
     * Update current sorting method for the achievements list.
     */
    fun setAchievementSortingMethod(method: Order.BaseComparator<Achievement>? = null) {
        sortingComparator.value = method ?: getNextSortingMethod()
    }

    /**
     * Increases current [index] or goes back to 0 is the index in the same
     * as the [sortingMethods] size.
     *
     * @return next sorting method based on the newly increased [index].
     */
    private fun getNextSortingMethod(): Order.BaseComparator<Achievement> {
        if (index == sortingMethods.keys.size) index = 0 else index++
        return sortingMethods[index] ?: AchievementSortedListImpl.DEFAULT_ORDER
    }

    /**
     * Getter for [sortingComparator] for observers.
     */
    fun getAchievementSortingMethod() = sortingComparator

    fun updatePalette(palette: Palette) {
        val lightMutedSwatch = palette.lightMutedSwatch
        val lightVibrantSwatch = palette.lightVibrantSwatch
        val darkVibrantSwatch = palette.darkVibrantSwatch
        val darkMutedSwatch = palette.darkMutedSwatch

        if (darkVibrantSwatch != null) {
            vibrantColor.postValue(darkVibrantSwatch)
        } else if (lightVibrantSwatch != null) {
            vibrantColor.postValue(lightVibrantSwatch)
        }

        if (darkMutedSwatch != null) {
            mutedColor.postValue(darkMutedSwatch)
        } else if (lightMutedSwatch != null) {
            mutedColor.postValue(lightMutedSwatch)
        } else if (darkVibrantSwatch != null && lightVibrantSwatch != null) {
            vibrantColor.postValue(lightVibrantSwatch)
            mutedColor.postValue(darkVibrantSwatch)
        }

        //        val vibrantRgb = palette.darkVibrantSwatch?.rgb
        //        val mutedRgb = palette.darkMutedSwatch?.rgb

        //        when {
        //            mutedRgb != null -> mutedRgb
        //            else -> vibrantRgb
        //        }?.let {rgb ->
        //            game.value?.data?.getAppId()?.let { appId ->
        //                gameRepo.getGameFromDbAsSingle(appId)
        //                    .subscribeOn(Schedulers.io())
        //                    .observeOn(Schedulers.io())
        //                    .subscribe({ game ->
        //                        game.colorPrimaryDark = rgb
        //                        gameRepo.update(game)
        //                    }, { error ->
        //                        Timber.e(error)
        //                    })
        //            }
    }

    fun setAppId(appId: String) {
        _appId.value = AppId(appId)
    }

    fun setGame(newGame: GameWithAchievements) {
        _appId.value = AppId(newGame.getAppId())
    }

    data class AppId(val id: String) {
        fun <T> ifExists(f: (String) -> LiveData<T>): LiveData<T> {
            return if (id.isBlank()) {
                AbsentLiveData.create()
            } else {
                f(id)
            }
        }
    }
}