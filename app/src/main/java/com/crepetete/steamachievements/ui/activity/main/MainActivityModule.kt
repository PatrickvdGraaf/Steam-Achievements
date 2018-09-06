package com.crepetete.steamachievements.ui.activity.main

import com.crepetete.steamachievements.data.repository.user.UserRepository
import dagger.Module
import dagger.Provides

@Module
class MainActivityModule {
    @Provides
    fun providesLoginView(mainActivity: MainActivity): MainView = mainActivity

    @Provides
    fun providesLoginPresenter(mainView: MainView,
                               userRepository: UserRepository): MainPresenter {
        return MainPresenter(mainView, userRepository)
    }
//    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
//    abstract fun contributeMainActivity(): MainActivity
}