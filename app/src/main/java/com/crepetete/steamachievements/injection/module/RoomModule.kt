package com.crepetete.steamachievements.injection.module

import android.app.Application
import androidx.room.Room
import com.crepetete.steamachievements.data.database.SteamDatabase
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.database.dao.PlayerDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RoomModule {
    @Provides
    @Singleton
    internal fun providePlayerDatabase(application: Application) = Room
            .databaseBuilder(application, SteamDatabase::class.java, "players.db")
            .build()

    @Provides
    @Singleton
    internal fun providePlayerDao(database: SteamDatabase): PlayerDao = database.playerDao()

    @Provides
    @Singleton
    internal fun provideGamesDao(database: SteamDatabase): GamesDao = database.gamesDao()

    @Provides
    @Singleton
    internal fun provideAchievementsDao(database: SteamDatabase): AchievementsDao =
            database.achievementsDao()
}