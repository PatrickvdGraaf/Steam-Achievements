package com.crepetete.steamachievements.injection

import android.app.Application
import android.content.Context
import com.crepetete.steamachievements.injection.module.*
import dagger.Module
import dagger.Provides


/**
 * We provide retrofit, okhttp, persistence db, shared pref etc here. There is an important detail
 * here. We have to add our subcomponents to AppModule.
 */
@Module(includes = [ViewModelModule::class, ApiModule::class, RepositoryModule::class,
    RoomModule::class, SharedPreferencesModule::class])
class AppModule {
    /**
     * Provides the Context
     * @return the Context to be provided
     */
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext
}