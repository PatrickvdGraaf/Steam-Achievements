package com.crepetete.steamachievements.di.submodules

import com.crepetete.steamachievements.di.MainActivityModule
import com.crepetete.steamachievements.ui.activity.game.GameActivity
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.activity.main.MainActivity
import com.crepetete.steamachievements.ui.activity.main.MainFragmentProvider
import com.crepetete.steamachievements.ui.activity.pager.TransparentPagerActivity
import com.crepetete.steamachievements.ui.activity.pager.TransparentPagerActivityModule
import com.crepetete.steamachievements.ui.fragment.achievement.pager.TransparentPagerActivityFragmentProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * This is a given module to dagger. We map all our activities here. And Dagger knows our activities
 * in compile time.
 */
@Suppress("unused")
@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun bindLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [
        (MainActivityModule::class),
        (MainFragmentProvider::class)])
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun bindGameActivity(): GameActivity

    @ContributesAndroidInjector(modules = [(TransparentPagerActivityModule::class),
        TransparentPagerActivityFragmentProvider::class])
    abstract fun bindTransparentPagerActivity(): TransparentPagerActivity

}