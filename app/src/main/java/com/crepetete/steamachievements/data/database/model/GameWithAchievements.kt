package com.crepetete.steamachievements.data.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game

class GameWithAchievements {
    @Embedded
    var game: Game? = null

    @Relation(parentColumn = "appId", entityColumn = "appId")
    var achievements: List<Achievement> = listOf()
}