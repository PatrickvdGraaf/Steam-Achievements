package com.crepetete.steamachievements.base

import android.app.Application
import android.support.annotation.NonNull
import android.util.Log
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.utils.SaCrashLibrary
import timber.log.Timber
import timber.log.Timber.DebugTree


class SteamAchievementsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    /** A tree which logs important information for crash reporting.  */
    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, @NonNull message: String, throwable: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            val t = throwable ?: Exception(message)
            SaCrashLibrary.log(priority, tag ?: "", message, t)
        }
    }
}