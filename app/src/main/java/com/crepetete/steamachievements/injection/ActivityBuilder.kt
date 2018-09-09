package com.crepetete.steamachievements.injection

import com.crepetete.steamachievements.ui.activity.game.GameActivity
import com.crepetete.steamachievements.ui.activity.game.GameActivityModule
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.activity.login.LoginActivityModule
import com.crepetete.steamachievements.ui.activity.main.MainActivity
import com.crepetete.steamachievements.ui.activity.main.MainActivityModule
import com.crepetete.steamachievements.ui.activity.main.MainFragmentProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector


/**
 * This is a given module to dagger. We map all our activities here. And Dagger knows our activities
 * in compile time.
 */
@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = [(LoginActivityModule::class)])
    abstract fun bindLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [(MainActivityModule::class),
        (MainFragmentProvider::class)])
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [(GameActivityModule::class)])
    abstract fun bindGameActivity(): GameActivity

}