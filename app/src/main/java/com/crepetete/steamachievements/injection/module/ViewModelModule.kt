package com.crepetete.steamachievements.injection.module

import android.arch.lifecycle.ViewModel
import com.crepetete.steamachievements.injection.annotation.ViewModelKey
import com.crepetete.steamachievements.ui.activity.game.GameViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(GameViewModel::class)
    abstract fun bindUserViewModel(gameViewModel: GameViewModel): ViewModel
}