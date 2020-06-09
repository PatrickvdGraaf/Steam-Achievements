package com.crepetete.steamachievements.presentation.di.submodules

import com.crepetete.steamachievements.data.repository.PreferencesRepositoryImpl
import com.crepetete.steamachievements.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module

@Module
abstract class SharedPreferencesModule {
    @Binds
    abstract fun provideStorage(storage: PreferencesRepositoryImpl): PreferencesRepository
}