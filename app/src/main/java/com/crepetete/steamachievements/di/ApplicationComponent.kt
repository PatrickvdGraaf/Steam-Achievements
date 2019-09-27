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
@Component(modules = [
    AndroidInjectionModule::class,
    ActivitiesModule::class,
    ApiModule::class,
    AppModule::class,
    FragmentModule::class,
    RoomModule::class,
    SharedPreferencesModule::class
])
interface ApplicationComponent {

    /* This is needed because SteamAchievementsApp has @Inject */
    fun inject(app: SteamAchievementsApp)

    @Component.Builder
    interface Builder {

        fun build(): ApplicationComponent

        /* provide Application instance into DI */
        @BindsInstance
        fun application(application: Application): Builder
    }
}