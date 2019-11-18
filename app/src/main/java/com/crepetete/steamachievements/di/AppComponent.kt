package com.crepetete.steamachievements.di

import android.content.Context
import com.crepetete.steamachievements.di.submodules.ApiModule
import com.crepetete.steamachievements.di.submodules.RoomModule
import com.crepetete.steamachievements.di.submodules.SharedPreferencesModule
import com.crepetete.steamachievements.ui.activity.achievements.pager.TransparentPagerActivity
import com.crepetete.steamachievements.ui.activity.game.GameActivity
import com.crepetete.steamachievements.ui.activity.game.fragment.achievement.pager.AchievementPagerFragment
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.activity.main.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.ui.activity.main.fragment.library.LibraryFragment
import com.crepetete.steamachievements.ui.activity.main.fragment.profile.ProfileFragment
import com.crepetete.steamachievements.ui.activity.splash.SplashScreenActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * The root in our dagger graph.
 */
@Singleton
@Component(modules = [
    ApiModule::class,
    RoomModule::class,
    SharedPreferencesModule::class
])
interface AppComponent {

    // Factory to create instances of the AppComponent
    @Component.Factory
    interface Factory {
        /* provide Context instance into DI using BindInstance. */
        fun create(@BindsInstance context: Context): AppComponent
    }

    // Classes that can be injected by this Component
    fun inject(activity: LoginActivity)

    fun inject(activity: GameActivity)

    fun inject(activity: TransparentPagerActivity)

    fun inject(activity: SplashScreenActivity)

    fun inject(fragment: AchievementsFragment)

    fun inject(fragment: LibraryFragment)

    fun inject(fragment: ProfileFragment)

    fun inject(fragment: AchievementPagerFragment)
}