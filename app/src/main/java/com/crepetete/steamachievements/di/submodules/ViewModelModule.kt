package com.crepetete.steamachievements.di.submodules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crepetete.steamachievements.di.helper.annotation.ViewModelKey
import com.crepetete.steamachievements.ui.activity.game.GameViewModel
import com.crepetete.steamachievements.ui.activity.login.LoginViewModel
import com.crepetete.steamachievements.ui.activity.pager.TransparentPagerViewModel
import com.crepetete.steamachievements.ui.fragment.achievement.pager.PagerFragmentViewModel
import com.crepetete.steamachievements.ui.fragment.library.LibraryViewModel
import com.crepetete.steamachievements.ui.fragment.profile.ProfileViewModel
import com.crepetete.steamachievements.viewmodel.ViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Module, helping Dagger inject ViewModelFactories.
 * Declare all ViewModels here.
 */
@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(LibraryViewModel::class)
    abstract fun postLibraryViewModel(viewModel: LibraryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameViewModel::class)
    abstract fun postGameViewModel(viewModel: GameViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransparentPagerViewModel::class)
    abstract fun postTransparentPagerViewModel(viewModel: TransparentPagerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PagerFragmentViewModel::class)
    abstract fun postPagerFragmentViewModel(viewModel: PagerFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun postLoginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun postProfileViewModel(viewModel: ProfileViewModel): ViewModel

    // Add more ViewModels here
}