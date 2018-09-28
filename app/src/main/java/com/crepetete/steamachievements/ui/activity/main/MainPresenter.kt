package com.crepetete.steamachievements.ui.activity.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.base.RefreshableFragment
import com.crepetete.steamachievements.data.repository.user.UserRepository
import com.crepetete.steamachievements.model.Player
import timber.log.Timber

class MainPresenter(mainView: MainView,
                    private val userRepository: UserRepository) : BasePresenter<MainView>(mainView) {

    private var _playerId: String? = null
    private val _player = MutableLiveData<Player>()

    val player: LiveData<Player>
        get() = _player

    override fun onViewCreated() {
        val id = _playerId
        if (id != null) {
            getPlayer(id)
        }
    }

    fun setPlayerId(playerId: String) {
        _playerId = playerId
        getPlayer(playerId)
    }

    private fun getPlayer(playerId: String) {
        disposable.add(userRepository.getPlayer(playerId)
                .subscribe({
                    _player.postValue(it)
                }, {
                    Timber.e(it)
                }))
    }

    fun onRefreshClicked() {
        val fragment = view.getCurrentFragment()
        if (fragment is RefreshableFragment<*>) {
            fragment.refresh()
        }
    }

    override fun onViewDestroyed() {
        disposable.dispose()
    }
}