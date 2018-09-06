package com.crepetete.steamachievements.ui.activity.login

import dagger.Subcomponent
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Subcomponent(modules = [(LoginActivityModule::class)])
interface LoginActivityComponent : AndroidInjector<LoginActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<LoginActivity>()
}