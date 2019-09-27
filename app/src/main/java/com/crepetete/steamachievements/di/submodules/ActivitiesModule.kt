package com.crepetete.steamachievements.di.submodules

import com.crepetete.steamachievements.di.annotation.ActivityScope
import com.crepetete.steamachievements.ui.activity.achievements.pager.TransparentPagerActivity
import com.crepetete.steamachievements.ui.activity.game.GameActivity
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.activity.main.MainActivity
import com.crepetete.steamachievements.ui.activity.splash.SplashScreenActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * We map all our activities here so Dagger knows them in compile time.
 *
 * Suppressed warning is allowed because Dagger will use the methods internally.
 */
@Suppress("unused")
@Module
abstract class ActivitiesModule {
    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindLoginActivity(): LoginActivity

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindGameActivity(): GameActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindTransparentPagerActivity(): TransparentPagerActivity

    @ContributesAndroidInjector
    abstract fun bindSplashScreenActivity(): SplashScreenActivity

}