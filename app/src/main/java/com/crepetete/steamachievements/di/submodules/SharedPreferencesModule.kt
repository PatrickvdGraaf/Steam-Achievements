package com.crepetete.steamachievements.di.submodules

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPreferencesModule {
    @Provides
    @Singleton
    fun getSharedPreferences(context: Context): SharedPreferences = context.getSharedPreferences(
        "UserPrefs",
        Context.MODE_PRIVATE
    )
}