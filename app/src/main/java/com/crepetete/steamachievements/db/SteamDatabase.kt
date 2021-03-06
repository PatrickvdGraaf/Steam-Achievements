package com.crepetete.steamachievements.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crepetete.steamachievements.db.dao.AchievementsDao
import com.crepetete.steamachievements.db.dao.GamesDao
import com.crepetete.steamachievements.db.dao.PlayerDao
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.BaseGameInfo
import com.crepetete.steamachievements.vo.Player

@Database(
    entities = [
        Player::class,
        BaseGameInfo::class,
        Achievement::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SteamDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao

    abstract fun gamesDao(): GamesDao

    abstract fun achievementsDao(): AchievementsDao
}