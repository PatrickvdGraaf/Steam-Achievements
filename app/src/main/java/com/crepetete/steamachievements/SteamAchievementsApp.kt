package com.crepetete.steamachievements

import android.app.Application
import com.crepetete.steamachievements.di.AppComponent
import com.crepetete.steamachievements.di.DaggerAppComponent
import com.crepetete.steamachievements.util.crashlytics.CrashlyticsTree
import timber.log.Timber
import timber.log.Timber.DebugTree

open class SteamAchievementsApp : Application() {

    // Instance of the AppComponent that will be used by all the Activities in the project
    val appComponent: AppComponent by lazy {
        initializeComponent()
    }

    override fun onCreate() {
        super.onCreate()

        // Plant Timber logging Tree.
        Timber.plant(if (BuildConfig.DEBUG) DebugTree() else CrashlyticsTree())
    }

    open fun initializeComponent(): AppComponent {
        // Creates an instance of AppComponent using its Factory constructor
        // We pass the applicationContext that will be used as Context in the graph
        return DaggerAppComponent.factory().create(applicationContext)
    }
}
