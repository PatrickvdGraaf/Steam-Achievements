package com.crepetete.steamachievements

import android.app.Activity
import android.app.Application
import com.crepetete.steamachievements.di.AppInjector
import com.crepetete.steamachievements.util.crashlytics.CrashlyticsTree
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject

open class SteamAchievementsApp : Application(), HasActivityInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        // Plant Timber logging Tree.
        Timber.plant(if (BuildConfig.DEBUG) DebugTree() else CrashlyticsTree())

        AppInjector.init(this)
    }

    override fun activityInjector() = dispatchingAndroidInjector
}