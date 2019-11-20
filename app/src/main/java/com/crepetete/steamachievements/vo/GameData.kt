package com.crepetete.steamachievements.vo

import android.content.Context
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.crepetete.steamachievements.R
import java.text.DecimalFormat

class GameData(private val game: Game) : BaseObservable() {

    @Bindable
    fun getName() = game.getName()

    /**
     * @return A base text representation of the players achievement completion.
     */
    @Bindable
    fun getAchievementsText() = when {
        hasCompletedAchievements() -> "${DecimalFormat("0.##").format(getPercentageCompleted())}% " +
                "(${getCompletedAchievements().size}/${game.achievements.size}) achievements." // Show a completion percentage
        game.achievements.isNotEmpty() -> "${game.achievements.size} achievements." // Only show total amount of achievements.
        else -> "No achievements" // Simply show that there are no achievements.
    }

    /**
     * @return Image URL for the games big banner image.
     */
    @Bindable
    fun getImageUrl() = game.getBannerUrl()

    /**
     * Formats total play tim Long into readable text using [toHours].
     *
     * @return A base text representation of the total playtime.
     */
    @Bindable
    fun getTotalPlayTimeString() = toHours(game.getPlaytime())

    /**
     * Formats recent play tim Long into readable text using [toHours].
     *
     * @return A base text representation of the recent playtime.
     */
    @Bindable
    fun getRecentPlaytimeString() = toHours(game.getRecentPlaytime())

    /**
     * @return a float representing a percentage (achieved achievements over total achievements).
     */
    @Bindable
    fun getPercentageCompleted(): Float {
        val achievedSize = getCompletedAchievements().size.toFloat()
        val totalSize = game.achievements.size.toFloat()
        return achievedSize / totalSize * 100F
    }

    /**
     * @return whether the player has collected all achievements.
     */
    fun isCompleted() = getAmountOfAchievements() == getAchievedAchievements().size && getAmountOfAchievements() > 0

    private fun getAmountOfAchievements() = game.achievements.size

    private fun getAchievedAchievements() = game.achievements.filter { achievement -> achievement.achieved }

    /**
     * Getter for a list of all achieved achievements.
     */
    private fun getCompletedAchievements() = game.achievements.filter { it.achieved }

    /**
     * Quick shortcut to check if the player has any achieved achievement for this game.
     */
    private fun hasCompletedAchievements() = game.achievements.any { it.achieved }

    private fun toHours(time: Long, context: Context? = null): String {
        val hours = time / 60
        val minutes = time % 60
        var hoursAbbr = "h"
        var minAbbr = "m"
        if (context != null) {
            hoursAbbr = context.getString(R.string.abbr_hours)
            minAbbr = context.getString(R.string.abbr_minutes)
        }

        return if (hours <= 0) {
            "$minutes$minAbbr"
        } else {
            "$hours$hoursAbbr, $minutes$minAbbr"
        }
    }
}