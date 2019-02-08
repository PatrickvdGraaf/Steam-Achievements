package com.crepetete.steamachievements.di.submodules

import com.crepetete.steamachievements.ui.activity.achievements.pager.TransparentPagerActivity
import com.crepetete.steamachievements.ui.activity.game.GameActivity
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.activity.main.MainActivity
import com.crepetete.steamachievements.ui.activity.splash.SplashScreenActivity
import com.crepetete.steamachievements.ui.fragment.achievement.pager.TransparentPagerActivityFragmentProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * This is a given module to dagger. We map all our activities here. And Dagger knows our activities
 * in compile time.
 *
 * Suppressed warning is allowed because Dagger will use the methods internally.
 */
@Suppress("unused")
@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun bindLoginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [MainFragmentProvider::class])
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun bindGameActivity(): GameActivity

    @ContributesAndroidInjector(modules = [TransparentPagerActivityFragmentProvider::class])
    abstract fun bindTransparentPagerActivity(): TransparentPagerActivity

    @ContributesAndroidInjector
    abstract fun bindSplashScreenActivity(): SplashScreenActivity

}