package com.crepetete.steamachievements.ui.activity.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.palette.graphics.Palette
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.util.AbsentLiveData
import com.crepetete.steamachievements.vo.GameWithAchievements
import javax.inject.Inject

class GameViewModel @Inject constructor(
    application: Application,
    private val gameRepo: GameRepository,
    private val achievementsRepo: AchievementsRepository
) : AndroidViewModel(application) {

    private val _appId = MutableLiveData<AppId>()
    val appId: LiveData<AppId>
        get() = _appId

    val game: LiveData<GameWithAchievements> = Transformations
        .switchMap(_appId) { id ->
            id.ifExists {
                achievementsRepo.updateAchievementsForGame(it)
                gameRepo.getGameWithAchFromDb(it)
            }
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