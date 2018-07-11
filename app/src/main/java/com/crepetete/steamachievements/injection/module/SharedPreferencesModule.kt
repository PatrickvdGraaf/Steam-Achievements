package com.crepetete.steamachievements.injection.module

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object SharedPreferencesModule {
    @Singleton
    @Provides
    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    }
}