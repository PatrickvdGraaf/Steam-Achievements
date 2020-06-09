package com.crepetete.steamachievements.presentation.di

import android.content.Context
import com.crepetete.steamachievements.presentation.activity.achievements.TransparentPagerActivity
import com.crepetete.steamachievements.presentation.activity.game.GameActivity
import com.crepetete.steamachievements.presentation.activity.login.LoginActivity
import com.crepetete.steamachievements.presentation.activity.splash.SplashScreenActivity
import com.crepetete.steamachievements.presentation.di.submodules.ApiModule
import com.crepetete.steamachievements.presentation.di.submodules.RoomModule
import com.crepetete.steamachievements.presentation.di.submodules.SharedPreferencesModule
import com.crepetete.steamachievements.presentation.fragment.achievement.pager.AchievementPagerFragment
import com.crepetete.steamachievements.presentation.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.presentation.fragment.library.LibraryFragment
import com.crepetete.steamachievements.presentation.fragment.profile.ProfileFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * The root in our dagger graph.
 */
@Singleton
@Component(
    modules = [
        ApiModule::class,
        RoomModule::class,
        SharedPreferencesModule::class
    ]
)
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