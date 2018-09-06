package com.crepetete.steamachievements.ui.activity.game

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent(modules = [(GameActivityModule::class)])
interface GameActivityComponent : AndroidInjector<GameActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<GameActivity>()
}