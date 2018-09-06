package com.crepetete.steamachievements.ui.activity.game

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.crepetete.steamachievements.data.repository.game.GameRepository
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import javax.inject.Inject

class GameViewModel @Inject constructor(private val gameRepo: GameRepository) : ViewModel() {
    private val _game = MutableLiveData<Game>()
    private val _achievements = MutableLiveData<List<Achievement>>()
    private var _appId: String? = null

    fun setAppId(appId: String) {
        if (_appId != appId) {
            gameRepo.getGame(appId)
        }
        _appId = appId
    }

    val game: LiveData<Game>
        get() = _game

    val achievements: LiveData<List<Achievement>>
        get() = _achievements
}