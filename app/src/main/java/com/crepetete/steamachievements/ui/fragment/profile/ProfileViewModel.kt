package com.crepetete.steamachievements.ui.fragment.profile

import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.UserRepository
import javax.inject.Inject

class ProfileViewModel @Inject constructor(userRepository: UserRepository): ViewModel() {
    val currentUser = userRepository.getCurrentPlayer()
}