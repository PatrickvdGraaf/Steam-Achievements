package com.crepetete.steamachievements.di.submodules

import com.crepetete.steamachievements.repository.storage.SharedPreferencesStorage
import com.crepetete.steamachievements.repository.storage.Storage
import dagger.Binds
import dagger.Module

@Module
abstract class SharedPreferencesModule {
    @Binds
    abstract fun provideStorage(storage: SharedPreferencesStorage): Storage
}