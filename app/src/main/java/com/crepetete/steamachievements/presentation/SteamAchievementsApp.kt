package com.crepetete.steamachievements.presentation

import android.app.Application
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.presentation.di.AppComponent
import com.crepetete.steamachievements.presentation.di.DaggerAppComponent
import com.crepetete.steamachievements.presentation.di.koin.dataModules
import com.crepetete.steamachievements.presentation.di.koin.domainModules
import com.crepetete.steamachievements.presentation.di.koin.presentationModules
import com.crepetete.steamachievements.util.crashlytics.CrashlyticsTree
import org.koin.android.ext.android.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

open class SteamAchievementsApp : Application() {

    // Instance of the AppComponent that will be used by all the Activities in the project
    val appComponent: AppComponent by lazy { initializeComponent() }

    override fun onCreate() {
        super.onCreate()

        // Plant Timber logging Tree.
        Timber.plant(if (BuildConfig.DEBUG) DebugTree() else CrashlyticsTree())

        // Initialize Koin
        startKoin(
            this@SteamAchievementsApp,
            listOf(dataModules, domainModules, presentationModules)
        )
    }

    open fun initializeComponent(): AppComponent {
        // Creates an instance of AppComponent using its Factory constructor
        // We pass the applicationContext that will be used as Context in the graph
        return DaggerAppComponent.factory().create(applicationContext)
    }
}
