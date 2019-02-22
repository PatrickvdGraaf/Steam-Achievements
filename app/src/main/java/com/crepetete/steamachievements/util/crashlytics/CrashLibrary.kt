package com.crepetete.steamachievements.util.crashlytics

import com.crashlytics.android.Crashlytics

object CrashLibrary {
    private const val CRASHLYTICS_KEY_PRIORITY = "priority"
    private const val CRASHLYTICS_KEY_TAG = "tag"
    private const val CRASHLYTICS_KEY_MESSAGE = "message"

    fun log(priority: Int, tag: String, message: String, t: Throwable) {
        Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority)
        Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag)
        Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message)
        Crashlytics.logException(t)
    }
}