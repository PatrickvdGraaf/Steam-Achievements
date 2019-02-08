package com.crepetete.steamachievements.di.submodules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.crepetete.steamachievements.di.annotation.ViewModelKey
import com.crepetete.steamachievements.di.factory.ViewModelFactory
import com.crepetete.steamachievements.ui.activity.achievements.pager.TransparentPagerViewModel
import com.crepetete.steamachievements.ui.activity.game.GameViewModel
import com.crepetete.steamachievements.ui.activity.login.AuthViewModel
import com.crepetete.steamachievements.ui.fragment.achievement.pager.PagerFragmentViewModel
import com.crepetete.steamachievements.ui.fragment.library.LibraryViewModel
import com.crepetete.steamachievements.ui.fragment.profile.ProfileViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Module, helping Dagger inject ViewModelFactories.
 * Declare all ViewModels here.
 *
 * Suppressed warning is allowed because the methods are used by Dagger internally.
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
    @ViewModelKey(AuthViewModel::class)
    abstract fun postAuthViewModel(viewModel: AuthViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    abstract fun postProfileViewModel(viewModel: ProfileViewModel): ViewModel

    // Add more ViewModels here
}