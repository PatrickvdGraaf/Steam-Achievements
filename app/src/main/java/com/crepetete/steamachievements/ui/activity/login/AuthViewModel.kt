package com.crepetete.steamachievements.ui.activity.login

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.*
import com.crepetete.steamachievements.repository.UserRepository
import com.crepetete.steamachievements.repository.resource.LiveResource
import com.crepetete.steamachievements.repository.resource.ResourceState
import com.crepetete.steamachievements.repository.storage.Storage
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenResponse
import org.json.JSONException
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel responsible for handling User login status.
 * It's used by the [LoginActivity] for logging in to the Steam
 */
@OpenForTesting
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storage: Storage
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

    private val _authState = MutableLiveData<AuthState?>()
    val authState: LiveData<AuthState?> = _authState

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

        enablePostAuthorizationFlows()
    }

    fun getAuthRequest(): AuthorizationRequest {
        val serviceConfiguration = AuthorizationServiceConfiguration(
            Uri.parse(
                "https://steamcommunity.com/openid/login" +
                        "?openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select" +
                        "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
                        "&openid.mode=checkid_setup" +
                        "&openid.ns=http://specs.openid.net/auth/2.0"
            ) /* auth endpoint */,
            Uri.parse("https://steamcommunity.com/openid") /* token endpoint */
        )

        val clientId = "511828570984-fuprh0cm7665emlne3rnf9pk34kkn86s.apps.googleusercontent.com"
        val redirectUri = Uri.parse("https://steamcommunity.com/openid/login")
        val builder = AuthorizationRequest.Builder(
            serviceConfiguration,
            clientId,
            AuthorizationRequest.RESPONSE_TYPE_CODE,
            redirectUri
        )
        builder.setScopes("profile")
        return builder.build()
    }

    /**
     * Extracts the playerId from the Steam url intercepted from the WebView.
     * It saves the ID to the SharedPreferences via the [userRepository] and also updates the
     * [_currentPlayerId] LiveData value.
     *
     * TODO Make the SharedPreferences return LiveData and let the _currentPlayerId listen to that.
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

    private fun enablePostAuthorizationFlows() {
        _authState.value = restoreAuthState()
    }

    /**
     * Exchanges the code, for the [TokenResponse].
     *
     * @param intent represents the [Intent] from the Custom Tabs or the System Browser.
     */
    private fun handleAuthorizationResponse(intent: Intent) {
        // code from the step 'Handle the Authorization Response' goes here.
    }

    private fun persistAuthState(authState: AuthState) {
        enablePostAuthorizationFlows()
    }

    private fun restoreAuthState(): AuthState? {
        storage.getAuthState()?.let { jsonString ->
            if (!TextUtils.isEmpty(jsonString)) {
                try {
                    return AuthState.fromJson(jsonString)
                } catch (jsonException: JSONException) {
                    // should never happen
                    Timber.e(jsonException)
                }
            }
        }
        return null
    }

    private fun <R> bindObserver(observer: MediatorLiveData<R?>?, source: LiveData<R?>) {
        observer?.apply {
            addSource(source) {
                postValue(it)
            }
        }
    }
}