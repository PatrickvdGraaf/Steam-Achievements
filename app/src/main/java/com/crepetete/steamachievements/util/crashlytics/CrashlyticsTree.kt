package com.crepetete.steamachievements.util.crashlytics

import android.util.Log
import timber.log.Timber

/**
 * Created at 19 January, 2019.
 */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (priority != Log.VERBOSE && priority != Log.DEBUG) {
            CrashLibrary.log(priority, tag ?: "", message, throwable ?: Exception(message))
        }
    }
}