package com.crepetete.steamachievements.ui.activity.login

import com.crepetete.steamachievements.data.repository.user.UserRepository
import dagger.Module
import dagger.Provides

/**
 * This module provides Login Activity related instances.
 */
@Module
class LoginActivityModule {
    @Provides
    fun providesLoginView(loginActivity: LoginActivity): LoginView = loginActivity

    @Provides
    fun providesLoginPresenter(loginView: LoginView,
                               userRepository: UserRepository): LoginPresenter {
        return LoginPresenter(loginView, userRepository)
    }
}