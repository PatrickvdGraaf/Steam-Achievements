package com.crepetete.steamachievements.di

import android.app.Application
import com.crepetete.steamachievements.SteamAchievementsApp
import com.crepetete.steamachievements.di.submodules.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

/**
 * The root in our dagger graph.
 */
@Singleton
/* Definition of a Dagger component. */
@Component(modules = [
    AndroidInjectionModule::class,
    ActivitiesModule::class,
    ApiModule::class,
    FragmentModule::class,
    RoomModule::class,
    SharedPreferencesModule::class,
    ViewModelModule::class
])
interface AppComponent {

    // Factory to create instances of the AppComponent
    @Component.Factory
    interface Factory {
        /* provide Application instance into DI */
        fun application(@BindsInstance application: Application): Factory
    }

    /* This is needed because SteamAchievementsApp has @Inject. */
    fun inject(app: SteamAchievementsApp)
}