package com.crepetete.steamachievements

import android.app.Activity
import androidx.annotation.NonNull
import android.util.Log
import com.crepetete.steamachievements.injection.DaggerAppComponent
import com.crepetete.steamachievements.utils.SaCrashLibrary
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject


open class SteamAchievementsApplication : DaggerApplication(), HasActivityInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    override fun activityInjector() = dispatchingAndroidInjector

    /** A tree which logs important information for crash reporting.  */
    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, @NonNull message: String,
                         throwable: Throwable?) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }

            val t = throwable ?: Exception(message)
            SaCrashLibrary.log(priority, tag ?: "", message, t)
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val appComponent = DaggerAppComponent.builder().application(this).build()
        appComponent.inject(this)
        return appComponent
    }
}