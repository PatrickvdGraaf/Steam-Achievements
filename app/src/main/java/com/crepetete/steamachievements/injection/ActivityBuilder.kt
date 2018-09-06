package com.crepetete.steamachievements.injection

import android.app.Activity
import com.crepetete.steamachievements.ui.activity.game.GameActivity
import com.crepetete.steamachievements.ui.activity.game.GameActivityComponent
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.activity.login.LoginActivityComponent
import com.crepetete.steamachievements.ui.activity.main.MainActivity
import com.crepetete.steamachievements.ui.activity.main.MainActivityComponent
import dagger.Binds
import dagger.Module
import dagger.android.ActivityKey
import dagger.android.AndroidInjector
import dagger.multibindings.IntoMap


/**
 * This is a given module to dagger. We map all our activities here. And Dagger knows our activities
 * in compile time.
 */
@Module
abstract class ActivityBuilder {
    @Binds
    @IntoMap
    @ActivityKey(LoginActivity::class)
    internal abstract fun bindLoginActivity(builder: LoginActivityComponent.Builder)
            : AndroidInjector.Factory<out Activity>

    @Binds
    @IntoMap
    @ActivityKey(MainActivity::class)
    internal abstract fun bindMainActivity(builder: MainActivityComponent.Builder)
            : AndroidInjector.Factory<out Activity>

    @Binds
    @IntoMap
    @ActivityKey(GameActivity::class)
    internal abstract fun bindGameActivity(builder: GameActivityComponent.Builder)
            : AndroidInjector.Factory<out Activity>

}