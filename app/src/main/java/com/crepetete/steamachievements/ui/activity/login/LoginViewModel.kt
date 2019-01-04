package com.crepetete.steamachievements.ui.activity.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.UserRepository
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.util.AbsentLiveData
import com.crepetete.steamachievements.vo.Player
import com.crepetete.steamachievements.vo.Resource
import javax.inject.Inject

@OpenForTesting
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val currentPlayerId = MutableLiveData<String?>()

    val currentPlayer: LiveData<Resource<Player>?> = Transformations.switchMap(currentPlayerId) {
        if (it != null) {
            userRepository.getPlayer(it)
        } else {
            AbsentLiveData.create()
        }
    }

    init {
        currentPlayerId.value = userRepository.getCurrentPlayerId()
    }

    fun parseIdFromUri(uri: Uri?) {
        // Extracts user id.
        val userAccountUrl = Uri.parse(uri?.getQueryParameter("openid.identity"))
        val playerId = userAccountUrl.lastPathSegment

        if (!playerId.isNullOrBlank()) {
            // Save the new Id
            currentPlayerId.value = playerId
            userRepository.putCurrentPlayerId(playerId!!)
        }
    }

    fun retry() {
        currentPlayerId.value?.let {
            currentPlayerId.value = it
        }
    }
}