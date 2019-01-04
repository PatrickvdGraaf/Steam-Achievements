package com.crepetete.steamachievements.vo

import android.content.Context
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.crepetete.steamachievements.util.extensions.getDaysFromNow
import com.crepetete.steamachievements.util.extensions.toHours
import com.squareup.moshi.Json
import java.text.DecimalFormat
import java.util.*

@Entity(tableName = "games",
        foreignKeys = [(ForeignKey(
                entity = Player::class,
                parentColumns = ["steamId"],
                childColumns = ["userId"],
                onDelete = CASCADE))])
data class Game(
        @PrimaryKey
        @Json(name = "appid")
        val appId: String,
        var userId: String,
        val name: String,
        @Json(name = "playtime_2weeks")
        val recentPlayTime: Long,
        @Json(name = "playtime_forever")
        val playTime: Long,
        @Json(name = "img_icon_url")
        val iconUrl: String,
        @Json(name = "img_logo_url")
        val logoUrl: String,
        var colorPrimaryDark: Int = 0,
        var lastUpdated: Long = Calendar.getInstance().time.time
) {
    @Ignore
    private var achievements: MutableList<Achievement>? = null

    fun achievementsWereAdded(): Boolean {
        return achievements != null
    }

    fun setAchievementsAdded() {
        if (achievements == null) {
            achievements = mutableListOf()
        }
    }

    fun setAchievements(achievements: List<Achievement>) {
        this.achievements = achievements.toMutableList()
    }

    fun addAchievements(achievements: List<Achievement>) {
        achievements.forEach { addAchievement(it) }
    }

    fun addAchievement(achievement: Achievement) {
        if (achievement.appId == appId) {
            if (achievements == null) {
                achievements = mutableListOf()
            }
            achievements!!.add(achievement)
        }
    }

    fun getFullLogoUrl() = "http://media.steampowered.com/steamcommunity/public/images/apps/$appId/$logoUrl.jpg"

    fun getRecentPlaytimeString(context: Context? = null) = if (recentPlayTime <= 0) {
        null
    } else {
        recentPlayTime.toHours(context)
    }

    fun getTotalPlayTimeString(context: Context? = null) = if (playTime <= 0) {
        null
    } else {
        playTime.toHours(context)
    }

    fun hasAchievements() = achievements?.isNotEmpty() ?: false

    fun getAmountOfAchievements(): Int = achievements?.size ?: 0

    fun getAchievedAchievements(): Int = achievements?.filter { it.achieved }?.size ?: 0

    fun getAchievementsText() = when {
        hasCompletedAchievements() ->
            "${getCompletedAchievements().size}/${achievements?.size} " +
                    "(${DecimalFormat("0.##")
                            .format(getPercentageCompleted())}%) achievements."
        hasAchievements() -> "${achievements?.size ?: 0} achievements."
        else -> ""
    }

    fun getPercentageCompleted(): Float {
        val achievementsSnapshot = achievements
        return if (achievementsSnapshot != null && achievementsSnapshot.isNotEmpty()) {
            val achievedSize = achievementsSnapshot.filter { it.achieved }.size.toFloat()
            val totalSize = achievementsSnapshot.size.toFloat()
            val percentage = achievedSize / totalSize * 100.0f
            percentage
        } else {
            0.0f
        }
    }

    private fun hasCompletedAchievements() = achievements?.any { it.achieved } ?: false

    private fun getCompletedAchievements() = achievements?.filter { it.achieved } ?: listOf()

    fun hasRecentPlaytime() = recentPlayTime > 0

    fun isCompleted() = getPercentageCompleted() >= 100L

    fun shouldUpdate(): Boolean {
        return Date(lastUpdated).getDaysFromNow() >= 14
    }
}