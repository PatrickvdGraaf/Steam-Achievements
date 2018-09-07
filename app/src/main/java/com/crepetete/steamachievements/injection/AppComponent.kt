package com.crepetete.steamachievements.injection

import android.app.Application
import com.crepetete.steamachievements.SteamAchievementsApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Android apps have one application class. That is why we have one application component.
 * This component is responsible for providing application scope instances (eg. OkHttp, Database,
 * SharedPrefs.). This Component is root of our dagger graph. Application component is providing
 * 3 module in our app.
 *
 * AndroidInjectionModule : We didn’t create this. It is an internal class in Dagger 2.10.
 * Provides our activities and fragments with given module.
 * ActivityBuilder : We created this module. This is a given module to dagger.
 * We map all our activities here.
 * And Dagger know our activities in compile time. In our app we have Main and Detail activity.
 * So we map both activities here.
 * AppModule: We provide retrofit, okhttp, persistence db, shared pref etc here. There is an
 * important detail here. We have to add our subcomponents to AppModule.
 */

@Singleton
@Component(modules = [(AndroidSupportInjectionModule::class),
    (AppModule::class),
    (ActivityBuilder::class)])
interface AppComponent : AndroidInjector<DaggerApplication> {
    fun inject(app: SteamAchievementsApplication)

    override fun inject(instance: DaggerApplication?)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }
}