package com.crepetete.steamachievements.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.database.dao.PlayerDao
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.model.Player
import com.crepetete.steamachievements.utils.Converters

@Database(entities = [(Player::class), (Game::class), (Achievement::class)], version = 1,
        exportSchema = false)
@TypeConverters(Converters::class)
abstract class SteamDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun gamesDao(): GamesDao
    abstract fun achievementsDao(): AchievementsDao
}