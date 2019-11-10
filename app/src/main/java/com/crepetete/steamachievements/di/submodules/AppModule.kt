package com.crepetete.steamachievements.di.submodules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module(includes = [ViewModelModule::class])
class AppModule {
    /**
     * Provides the application Context to a ViewModel.
     */
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext
}