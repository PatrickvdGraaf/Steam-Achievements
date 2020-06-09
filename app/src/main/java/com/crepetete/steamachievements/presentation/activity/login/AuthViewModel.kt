package com.crepetete.steamachievements.presentation.activity.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.crepetete.data.helper.LiveResource
import com.crepetete.data.helper.ResourceState
import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.domain.usecases.player.GetCurrentPlayerIdUseCase
import com.crepetete.steamachievements.domain.usecases.player.GetPlayerUseCase
import com.crepetete.steamachievements.domain.usecases.player.SaveCurrentPlayerIdUserCase
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.util.extensions.bindObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for handling User login status.
 * It's used by the [LoginActivity] for logging in to the Steam
 */
@OpenForTesting
class AuthViewModel(
    private val getCurrentPlayerIdUseCase: GetCurrentPlayerIdUseCase,
    private val getPlayerUseCase: GetPlayerUseCase,
    private val saveCurrentPlayerIdUserCase: SaveCurrentPlayerIdUserCase
) : ViewModel() {

    private val mainJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mainJob)
    private var fetchPlayerJob: Job? = null

    private var _playerLiveResource: LiveResource<Player>? = null
    private val _player = MediatorLiveData<Player?>()
    private val _playerLoadingState = MediatorLiveData<@ResourceState Int?>()
    private val _playerLoadingError = MediatorLiveData<Exception?>()

    private val _currentPlayerId = MediatorLiveData<String?>().apply {
        value = getCurrentPlayerIdUseCase(Player.INVALID_ID)
    }
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
                        getPlayerUseCase(id).apply {
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

    /**
     * Extracts the playerId from the Steam url intercepted from the WebView.
     * It saves the ID to the SharedPreferences and also updates the [_currentPlayerId] LiveData
     * value.
     *
     * TODO: Make the SharedPreferences return LiveData and let the _currentPlayerId listen to that.
     */
    fun parseIdFromUri(uri: Uri?) {
        val userAccountUrl = Uri.parse(uri?.getQueryParameter("openid.identity") ?: "")
        val playerId = userAccountUrl.lastPathSegment

        if (!playerId.isNullOrBlank()) {
            _currentPlayerId.value = playerId
            saveCurrentPlayerIdUserCase(playerId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        mainJob.cancel()
    }
}