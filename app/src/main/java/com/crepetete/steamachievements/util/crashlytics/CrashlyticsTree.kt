package com.crepetete.steamachievements.util.crashlytics

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * Created at 19 January, 2019.
 */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        if (priority != Log.VERBOSE && priority != Log.DEBUG) {
            Crashlytics.setInt("priority", priority)
            Crashlytics.setString("message", message)
            tag?.let {
                Crashlytics.setString("tag", tag)
            }

            Crashlytics.logException(throwable ?: Exception(message))
        }
    }
}