package com.crepetete.steamachievements.injection.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.crepetete.steamachievements.injection.helper.ViewModelFactory
import com.crepetete.steamachievements.injection.helper.annotation.ViewModelKey
import com.crepetete.steamachievements.ui.activity.game.GameViewModel
import com.crepetete.steamachievements.ui.activity.pager.TransparentPagerViewModel
import com.crepetete.steamachievements.ui.fragment.achievement.pager.PagerFragmentViewModel
import com.crepetete.steamachievements.ui.fragment.library.LibraryViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Module, helping Dagger inject ViewModelFactories.
 * Declare all ViewModels here.
 */
@Module
abstract class ViewModelModule {
    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(LibraryViewModel::class)
    internal abstract fun postLibraryViewModel(viewModel: LibraryViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GameViewModel::class)
    internal abstract fun postGameViewModel(viewModel: GameViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TransparentPagerViewModel::class)
    internal abstract fun postTransparentPagerViewModel(viewModel: TransparentPagerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PagerFragmentViewModel::class)
    internal abstract fun postPagerFragmentViewModel(viewModel: PagerFragmentViewModel): ViewModel

    //Add more ViewModels here
}