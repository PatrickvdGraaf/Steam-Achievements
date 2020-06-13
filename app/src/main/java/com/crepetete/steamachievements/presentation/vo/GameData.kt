package com.crepetete.steamachievements.presentation.vo

import android.content.Context
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.domain.model.Game
import java.text.DecimalFormat

/**
 * A representation of a Game object used in the activity_game view binding. Provides utility
 * methods that allow the binding to fill in all the requested information from the [game] object in
 * readable form.
 */
class GameData(private val game: Game) {

    fun getName() = game.getName()

    /**
     * @return A base text representation of the players achievement completion.
     */
    fun getAchievementsText() = when {
        hasCompletedAchievements() -> "${DecimalFormat("0.##").format(getPercentageCompleted())}% " +
                "(${getCompletedAchievements().size}/${game.achievements.size}) achievements." // Show a completion percentage
        game.achievements.isNotEmpty() -> "${game.achievements.size} achievements." // Only show total amount of achievements.
        else -> "No achievements" // Simply show that there are no achievements.
    }

    /**
     * @return Image URL for the games big banner image.
     */
    fun getImageUrl() = game.getBannerUrl()

    /**
     * Formats total play tim Long into readable text using [toHours].
     *
     * @return A base text representation of the total playtime.
     */
    fun getTotalPlayTimeString() = toHours(game.getPlaytime())

    /**
     * Formats recent play tim Long into readable text using [toHours].
     *
     * @return A base text representation of the recent playtime.
     */
    fun getRecentPlaytimeString() = toHours(game.getRecentPlaytime())

    /**
     * @return a float representing a percentage (achieved achievements over total achievements).
     */
    fun getPercentageCompleted(): Float {
        val achievedSize = getCompletedAchievements().size.toFloat()
        val totalSize = game.achievements.size.toFloat()
        return achievedSize / totalSize * 100F
    }

    /**
     * @return whether the player has collected all achievements.
     */
    fun isCompleted() =
        getAmountOfAchievements() == getAchievedAchievements().size && getAmountOfAchievements() > 0

    fun getAchievements() = game.achievements

    private fun getAmountOfAchievements() = game.achievements.size

    private fun getAchievedAchievements() =
        game.achievements.filter { achievement -> achievement.achieved }

    /**
     * Getter for a list of all achieved achievements.
     */
    private fun getCompletedAchievements() = game.achievements.filter { it.achieved }

    /**
     * Quick shortcut to check if the player has any achieved achievement for this game.
     */
    private fun hasCompletedAchievements() = game.achievements.any { it.achieved }

    private fun toHours(time: Int, context: Context? = null): String {
        val hours = time / 60
        val minutes = time % 60
        var hoursAbbr = "h"
        var minAbbr = "m"
        if (context != null) {
            hoursAbbr = context.getString(R.string.abbr_hours)
            minAbbr = context.getString(R.string.abbr_minutes)
        }

        return if (hours <= 0 && minutes <= 0) {
            return ""
        } else if (hours <= 0) {
            "$minutes$minAbbr"
        } else {
            "$hours$hoursAbbr, $minutes$minAbbr"
        }
    }

    fun hasResentPlaytime(): Boolean {
        return game.getRecentPlaytime() > 0
    }
}