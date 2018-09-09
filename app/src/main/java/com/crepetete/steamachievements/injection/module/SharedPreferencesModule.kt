package com.crepetete.steamachievements.injection.module

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class SharedPreferencesModule {
    @Provides
    @Singleton
    fun getSharedPreferences(context: Context) = context.getSharedPreferences("UserPrefs",
            Context.MODE_PRIVATE)
}