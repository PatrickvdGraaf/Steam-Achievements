package com.crepetete.steamachievements.vo

import android.content.Context
import androidx.room.Embedded
import androidx.room.Relation
import com.crepetete.steamachievements.util.extensions.toHours
import java.text.DecimalFormat

/**
 * Combined database model with a game and its achievements.
 */
class GameWithAchievements(
    @Embedded
    var game: Game? = null,

    @Relation(parentColumn = "appId", entityColumn = "appId", entity = Achievement::class)
    var achievements: List<Achievement> = listOf()
) {

    /*
     Game Methods.
      */

    /**
     * @return Image URL for the games big banner image.
     */
    fun getFullLogoUrl() = "http://media.steampowered.com/steamcommunity/public/images/apps/${getAppId()}/${getBannerUrl()}.jpg"

    /**
     * Formats recent play tim Long into readable text using [toHours].
     *
     * TODO move this logic to a view.
     *
     * @return A base text representation of the recent playtime.
     */
    fun getRecentPlaytimeString(context: Context? = null) = game?.recentPlayTime?.toHours(context) ?: ""

    /**
     * Formats total play tim Long into readable text using [toHours].
     *
     * TODO move this logic to a view.
     *
     * @return A base text representation of the total playtime.
     */
    fun getTotalPlayTimeString(context: Context? = null) = game?.playTime?.toHours(context)

    /**
     * @return whether the player has collected all achievements.
     */
    fun isCompleted() = getPercentageCompleted() == 1F

    fun setPrimaryColor(rgb: Int) {
        game?.colorPrimaryDark = rgb
    }

    /*
     Achievement Methods.
      */

    /**
     * TODO move this logic to a view.
     * @return A base text representation of the players achievement completion.
     */
    fun getAchievementsText() = when {
        // Show a completion percentage
        hasCompletedAchievements() ->
            "${getCompletedAchievements().size}/${achievements.size} (${DecimalFormat("0.##")
                .format(getPercentageCompleted())}%) achievements."
        // Only show total amount of achievements.
        achievements.isNotEmpty() -> "${achievements.size} achievements."
        else -> ""
    }

    /**
     * TODO move this logic to a view.
     * @return a float representing a percentage (achieved achievements over total achievements).
     */
    fun getPercentageCompleted(): Float {
        val achievedSize = getCompletedAchievements().size.toFloat()
        val totalSize = achievements.size.toFloat()
        return achievedSize / totalSize * 100F
    }

    /**
     * Quick shortcut to check if the player has any achieved achievement for this game.
     */
    private fun hasCompletedAchievements() = achievements.any { it.achieved }

    /**
     * Getter for a list of all achieved achievements.
     */
    private fun getCompletedAchievements() = achievements.filter { it.achieved }

    fun getAmountOfAchievements() = achievements.size

    fun getAchievedAchievements() = achievements.filter { achievement -> achievement.achieved }

    fun hasAchievements() = getAmountOfAchievements() > 0

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