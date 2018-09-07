package com.crepetete.steamachievements.ui.activity.game

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.repository.achievement.AchievementsRepository
import com.crepetete.steamachievements.data.repository.game.GameRepository
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.utils.AbsentLiveData
import com.crepetete.steamachievements.utils.resource.Resource
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class GameViewModel @Inject constructor(private var gameRepo: GameRepository,
                                        private val achievementsDao: AchievementsDao,
                                        private var achievementsRepo: AchievementsRepository) : ViewModel() {
    private var disposable: CompositeDisposable = CompositeDisposable()

    private val _appId = MutableLiveData<AppId>()
    val appId: LiveData<AppId>
        get() = _appId

    val gameLive: LiveData<Game> = Transformations
            .switchMap(_appId) { id ->
                id.ifExists {
                    gameRepo.getGame(it)
                }
            }

    val achievementsLive: LiveData<Resource<List<Achievement>>> = Transformations
            .switchMap(_appId) { id ->
                id.ifExists {
                    achievementsRepo.loadAchievementsForGame(it)
                }
            }

    fun setAppId(appId: String) {
        val update = AppId(appId)
        if (_appId.value == update) {
            return
        }
        _appId.value = update
    }

    override fun onCleared() {
        disposable.dispose()
        super.onCleared()
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