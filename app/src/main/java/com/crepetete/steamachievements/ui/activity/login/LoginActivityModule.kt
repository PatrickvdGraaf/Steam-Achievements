package com.crepetete.steamachievements.ui.activity.login

import com.crepetete.steamachievements.data.repository.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

/**
 * This module provides Login Activity related instances.
 */
@Module
abstract class LoginActivityModule {
    @Binds
    abstract fun providesLoginView(loginActivity: LoginActivity): LoginView

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesLoginPresenter(loginView: LoginView,
                                   userRepository: UserRepository) = LoginPresenter(loginView, userRepository)
    }
}