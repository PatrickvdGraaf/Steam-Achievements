package com.crepetete.steamachievements.ui.activity.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.UserRepository
import com.crepetete.steamachievements.repository.resource.LiveResource
import com.crepetete.steamachievements.repository.resource.ResourceState
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.util.extensions.bindObserver
import com.crepetete.steamachievements.vo.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for handling User login status.
 * It's used by the [LoginActivity] for logging in to the Steam
 */
@OpenForTesting
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val mainJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mainJob)
    private var fetchPlayerJob: Job? = null

    private var _playerLiveResource: LiveResource<Player>? = null
    private val _player = MediatorLiveData<Player?>()
    private val _playerLoadingState = MediatorLiveData<@ResourceState Int?>()
    private val _playerLoadingError = MediatorLiveData<Exception?>()

    private val _currentPlayerId = MediatorLiveData<String?>()
    val currentPlayerId: LiveData<String?> = _currentPlayerId

    /*
     * More information on this setup:
     * https://developer.android.com/topic/libraries/architecture/coroutines.
     */
    val currentPlayer: LiveData<Player?>
        get() {
            Transformations.map(_currentPlayerId) { id ->
                if (id != null) {
                    uiScope.launch {
                        userRepository.getPlayer(id).apply {
                            _playerLiveResource = this
                            fetchPlayerJob = this.job
                            bindObserver(_player, this.data)
                            bindObserver(_playerLoadingState, this.state)
                            bindObserver(_playerLoadingError, this.error)
                        }
                    }
                }
            }
            return _player
        }

    val idLoadingState: LiveData<@ResourceState Int?> = _playerLoadingState
    val idLoadingError: LiveData<Exception?> = _playerLoadingError

    init {
        _currentPlayerId.value = userRepository.getCurrentPlayerId()

        Transformations.map(_currentPlayerId) { id ->
            if (id != null) {
                uiScope.launch {
                    userRepository.getPlayer(id).apply {
                        _playerLiveResource = this
                        fetchPlayerJob = this.job
                        bindObserver(_player, this.data)
                        bindObserver(_playerLoadingState, this.state)
                        bindObserver(_playerLoadingError, this.error)
                    }
                }
            }
        }
    }

    /**
     * Extracts the playerId from the Steam url intercepted from the WebView.
     * It saves the ID to the SharedPreferences via the [userRepository] and also updates the
     * [_currentPlayerId] LiveData value.
     *
     * TODO: Make the SharedPreferences return LiveData and let the _currentPlayerId listen to that.
     */
    fun parseIdFromUri(uri: Uri?) {
        val userAccountUrl = Uri.parse(uri?.getQueryParameter("openid.identity") ?: "")
        val playerId = userAccountUrl.lastPathSegment

        if (!playerId.isNullOrBlank()) {
            _currentPlayerId.value = playerId
            userRepository.putCurrentPlayerId(playerId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mainJob.cancel()
    }
}