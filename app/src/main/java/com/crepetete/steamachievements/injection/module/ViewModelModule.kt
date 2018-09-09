package com.crepetete.steamachievements.injection.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.crepetete.steamachievements.injection.helper.ViewModelFactory
import com.crepetete.steamachievements.injection.helper.annotation.ViewModelKey
import com.crepetete.steamachievements.ui.activity.game.GameViewModel
import com.crepetete.steamachievements.ui.fragment.library.LibraryViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

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

    //Add more ViewModels here
}