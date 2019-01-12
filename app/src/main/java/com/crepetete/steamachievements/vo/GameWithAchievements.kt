package com.crepetete.steamachievements.vo

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Combined database model with a game and its achievements.
 */
class GameWithAchievements(
    @Embedded
    var game: Game? = null,

    @Relation(parentColumn = "appId", entityColumn = "appId", entity = Achievement::class)
    var achievements: List<Achievement> = listOf()
) {

    fun getPercentageCompleted(): Float {
        val achievedSize = achievements.filter { it.achieved }.size.toFloat()
        val totalSize = achievements.size.toFloat()
        return achievedSize / totalSize * 100F
    }

    /*
     Game Methods.
      */
    fun setPrimaryColor(rgb: Int) {
        game?.colorPrimaryDark = rgb
    }

    fun getAmountOfAchievements() = achievements.size

    fun getAchievedAchievements() = achievements.filter { achievement -> achievement.achieved }

    /*
    Game Getters.
    */
    fun getRecentPlaytime() = game?.recentPlayTime ?: 0

    fun getPrimaryColor() = game?.colorPrimaryDark ?: 0
    fun getAppId() = game?.appId ?: ""
    fun getName() = game?.name ?: ""
    fun getPlaytime() = game?.playTime ?: 0L
    fun getIconUrl() = game?.iconUrl ?: ""
    fun getBannerUrl() = game?.logoUrl ?: ""

}