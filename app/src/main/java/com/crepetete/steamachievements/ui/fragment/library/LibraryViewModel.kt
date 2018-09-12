package com.crepetete.steamachievements.ui.fragment.library

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.crepetete.steamachievements.data.database.model.GameWithAchievements
import com.crepetete.steamachievements.data.repository.achievement.AchievementsRepository
import com.crepetete.steamachievements.data.repository.game.GameRepository
import com.crepetete.steamachievements.utils.AbsentLiveData
import com.crepetete.steamachievements.utils.resource.Resource
import javax.inject.Inject


class LibraryViewModel @Inject constructor(private var gameRepo: GameRepository,
                                           private var achievementsRepository: AchievementsRepository) : ViewModel() {
    private val _userId = MutableLiveData<UserId>()
    val userId: LiveData<UserId>
        get() = _userId

    val games: LiveData<Resource<List<GameWithAchievements>>> = Transformations
            .switchMap(_userId) { id ->
                id.ifExists {
                    gameRepo.getGames(it)
                }
            }

    fun setAppId(appId: String) {
        val update = UserId(appId)
        if (_userId.value == update) {
            return
        }
        _userId.value = update
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