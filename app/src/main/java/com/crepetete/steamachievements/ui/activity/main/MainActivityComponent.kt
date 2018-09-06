package com.crepetete.steamachievements.ui.activity.main

import com.crepetete.steamachievements.injection.FragmentBuilder
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent(modules = [(MainActivityModule::class), (FragmentBuilder::class)])
interface MainActivityComponent : AndroidInjector<MainActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MainActivity>()
}