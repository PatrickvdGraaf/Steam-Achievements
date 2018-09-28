package com.crepetete.steamachievements.ui.activity.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.palette.graphics.Palette
import com.crepetete.steamachievements.data.repository.achievement.AchievementsRepository
import com.crepetete.steamachievements.data.repository.game.GameRepository
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.utils.AbsentLiveData
import com.crepetete.steamachievements.utils.resource.Resource
import javax.annotation.Nonnull
import javax.inject.Inject

class GameViewModel @Inject constructor(@Nonnull application: Application,
                                        private var gameRepo: GameRepository,
                                        private var achievementsRepo: AchievementsRepository)
    : AndroidViewModel(application) {

    private val _appId = MutableLiveData<AppId>()
    val appId: LiveData<AppId>
        get() = _appId

    val game: LiveData<Game> = Transformations
            .switchMap(_appId) { id ->
                id.ifExists {
                    gameRepo.getGameFromDb(it)
                }
            }

    private val achievements: LiveData<Resource<List<Achievement>>> = Transformations
            .switchMap(_appId) { id ->
                id.ifExists {
                    achievementsRepo.loadAchievementsForGame(it)
                }
            }

    private val updatedAchievements: LiveData<Resource<List<Achievement>>> = Transformations
            .switchMap(achievements) {
                val id = _appId.value?.id
                val achievements = it.data
                if (id != null && achievements != null) {
                    return@switchMap achievementsRepo.getAchievedStatusForAchievementsForGame(id,
                            achievements)
                }
                return@switchMap AbsentLiveData.create<Resource<List<Achievement>>>()
            }

    val finalAchievements: LiveData<Resource<List<Achievement>>> = Transformations
            .switchMap(updatedAchievements) {
                val id = _appId.value?.id
                val achievements = it?.data
                if (id != null && achievements != null) {
                    return@switchMap achievementsRepo.getGlobalAchievementStats(id, achievements)
                }
                return@switchMap AbsentLiveData.create<Resource<List<Achievement>>>()
            }


    val vibrantColor: MutableLiveData<Palette.Swatch> = MutableLiveData()
    val mutedColor: MutableLiveData<Palette.Swatch> = MutableLiveData()

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
    }

    fun setAppId(appId: String) {
        val update = AppId(appId)
        if (_appId.value == update) {
            return
        }
        _appId.value = update
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