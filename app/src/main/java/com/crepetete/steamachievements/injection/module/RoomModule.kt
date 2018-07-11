package com.crepetete.steamachievements.injection.module

import android.app.Application
import android.arch.persistence.room.Room
import com.crepetete.steamachievements.data.database.SteamDatabase
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.database.dao.PlayerDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object RoomModule {
    @Singleton
    @Provides
    internal fun providePlayerDatabase(application: Application) = Room
            .databaseBuilder(application, SteamDatabase::class.java, "players.db")
            .build()

    @Singleton
    @Provides
    internal fun providePlayerDao(database: SteamDatabase): PlayerDao = database.playerDao()

    @Singleton
    @Provides
    internal fun provideGamesDao(database: SteamDatabase): GamesDao = database.gamesDao()

    @Singleton
    @Provides
    internal fun provideAchievementsDao(database: SteamDatabase): AchievementsDao =
            database.achievementsDao()
}