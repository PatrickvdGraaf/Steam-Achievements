package com.crepetete.steamachievements.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.database.dao.NewsDao
import com.crepetete.steamachievements.data.database.dao.PlayerDao
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Player

@Database(
    entities = [
        Player::class,
        BaseGameInfo::class,
        Achievement::class,
        NewsItem::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SteamDatabase : RoomDatabase() {

    companion object {
        fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context,
                SteamDatabase::class.java,
                BuildConfig.DB_NAME
            ).fallbackToDestructiveMigration().build()
    }

    abstract fun playerDao(): PlayerDao

    abstract fun gamesDao(): GamesDao

    abstract fun achievementsDao(): AchievementsDao

    abstract fun newsDao(): NewsDao
}