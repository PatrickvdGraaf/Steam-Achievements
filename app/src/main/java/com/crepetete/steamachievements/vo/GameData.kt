package com.crepetete.steamachievements.vo

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.crepetete.steamachievements.util.extensions.toHours
import java.text.DecimalFormat

class GameData(private val game: GameWithAchievements) : BaseObservable() {

    @Bindable
    fun getName() = game.getName()

    /**
     * @return A base text representation of the players achievement completion.
     */
    @Bindable
    fun getAchievementsText() = when {
        // Show a completion percentage
        hasCompletedAchievements() -> "${getCompletedAchievements().size}/${game.achievements.size} (${DecimalFormat("0.##").format(getPercentageCompleted())}%) achievements."
        // Only show total amount of achievements.
        game.achievements.isNotEmpty() -> "${game.achievements.size} achievements."
        else -> "No achievements"
    }

    @Bindable
    fun getTotalPlaytime() = game.getPlaytime()

    @Bindable
    fun getRecentPlaytime() = game.getRecentPlaytime()

    /**
     * @return Image URL for the games big banner image.
     */
    @Bindable
    fun getImageUrl() = "http://media.steampowered.com/steamcommunity/public/images/apps/${game.getAppId()}/${game.getBannerUrl()}.jpg"

    /**
     * Formats total play tim Long into readable text using [toHours].
     *
     * @return A base text representation of the total playtime.
     */
    @Bindable
    fun getTotalPlayTimeString() = game.getPlaytime().toHours()

    /**
     * Formats recent play tim Long into readable text using [toHours].
     *
     * @return A base text representation of the recent playtime.
     */
    @Bindable
    fun getRecentPlaytimeString() = game.getRecentPlaytime().toHours()

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

    fun hasAchievements() = getAmountOfAchievements() > 0

    /**
     * Getter for a list of all achieved achievements.
     */
    private fun getCompletedAchievements() = game.achievements.filter { it.achieved }

    /**
     * Quick shortcut to check if the player has any achieved achievement for this game.
     */
    private fun hasCompletedAchievements() = game.achievements.any { it.achieved }
}