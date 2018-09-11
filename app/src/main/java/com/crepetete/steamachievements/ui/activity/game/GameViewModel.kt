package com.crepetete.steamachievements.ui.activity.game

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.crepetete.steamachievements.data.repository.achievement.AchievementsRepository
import com.crepetete.steamachievements.data.repository.game.GameRepository
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.utils.AbsentLiveData
import com.crepetete.steamachievements.utils.resource.Resource
import javax.inject.Inject

class GameViewModel @Inject constructor(private var gameRepo: GameRepository,
                                        private var achievementsRepo: AchievementsRepository)
    : ViewModel() {

    private val _appId = MutableLiveData<AppId>()
    val appId: LiveData<AppId>
        get() = _appId

    val game: LiveData<Game> = Transformations
            .switchMap(_appId) { id ->
                id.ifExists {
                    gameRepo.getGameFromDb(it)
                }
            }

    val achievements: LiveData<Resource<List<Achievement>>> = Transformations
            .switchMap(_appId) { id ->
                id.ifExists {
                    achievementsRepo.loadAchievementsForGame(it)
                }
            }

    val updatedAchievements: LiveData<Resource<List<Achievement>>> = Transformations
            .switchMap(achievements) {
                val id = _appId.value?.id
                val achievements = it.data
                if (id != null && achievements != null) {
                    return@switchMap achievementsRepo.getAchievedStatusForAchievementsForGame(id,
                            achievements)
                }
                return@switchMap AbsentLiveData.create<Resource<List<Achievement>>>()
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