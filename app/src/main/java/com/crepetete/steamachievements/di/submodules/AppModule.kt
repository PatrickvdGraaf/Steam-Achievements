package com.crepetete.steamachievements.di.submodules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides

@Module(includes = [ViewModelModule::class])
class AppModule {
    /**
     * Provides the Context
     * @return the Context to be provided
     */
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext
}