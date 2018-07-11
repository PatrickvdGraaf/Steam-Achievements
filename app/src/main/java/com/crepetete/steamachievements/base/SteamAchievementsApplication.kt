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
        override fun log(priority: Int, tag: String?, @NonNull message: String, t: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            SaCrashLibrary.log(priority, tag ?: "", message)

            if (t != null) {
                if (priority == Log.ERROR) {
                    SaCrashLibrary.logError(t)
                } else if (priority == Log.WARN) {
                    SaCrashLibrary.logWarning(t)
                }
            }
        }
    }
}