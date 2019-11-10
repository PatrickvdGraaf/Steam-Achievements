package com.crepetete.steamachievements.di.submodules

import android.app.Application
import androidx.room.Room
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.db.SteamDatabase
import com.crepetete.steamachievements.db.dao.AchievementsDao
import com.crepetete.steamachievements.db.dao.GamesDao
import com.crepetete.steamachievements.db.dao.PlayerDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule {
    @Provides
    @Singleton
    internal fun providePlayerDatabase(context: Application) = Room.databaseBuilder(
        context,
        SteamDatabase::class.java,
        BuildConfig.DB_NAME
    ).fallbackToDestructiveMigration() // TODO remove this before releasing.
        .build()

    @Provides
    internal fun providePlayerDao(database: SteamDatabase): PlayerDao = database.playerDao()

    @Provides
    internal fun provideGamesDao(database: SteamDatabase): GamesDao = database.gamesDao()

    @Provides
    @Singleton
    internal fun provideAchievementsDao(database: SteamDatabase): AchievementsDao = database.achievementsDao()
}