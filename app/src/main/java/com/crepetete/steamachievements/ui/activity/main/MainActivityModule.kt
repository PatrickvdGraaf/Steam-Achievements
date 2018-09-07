package com.crepetete.steamachievements.ui.activity.main

import com.crepetete.steamachievements.data.repository.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class MainActivityModule {
    @Binds
    abstract fun providesLoginView(mainActivity: MainActivity): MainView

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesLoginPresenter(mainView: MainView,
                                   userRepository: UserRepository) = MainPresenter(mainView, userRepository)
    }
}