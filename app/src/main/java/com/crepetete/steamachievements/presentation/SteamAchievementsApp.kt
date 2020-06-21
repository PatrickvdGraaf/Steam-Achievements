package com.crepetete.steamachievements.presentation

import android.app.Application
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.presentation.di.dataModules
import com.crepetete.steamachievements.presentation.di.domainModules
import com.crepetete.steamachievements.presentation.di.presentationModules
import com.crepetete.steamachievements.util.crashlytics.CrashlyticsTree
import org.koin.android.ext.android.startKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

open class SteamAchievementsApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initializeTimber()
        initializeKion()
    }

    private fun initializeTimber() {
        Timber.plant(if (BuildConfig.DEBUG) DebugTree() else CrashlyticsTree())
    }

    private fun initializeKion() {
        startKoin(
            this@SteamAchievementsApp,
            listOf(
                dataModules,
                domainModules,
                presentationModules
            )
        )
    }
}
