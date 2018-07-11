package com.crepetete.steamachievements.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import com.crepetete.steamachievements.utils.getDaysFromNow
import com.crepetete.steamachievements.utils.toHours
import com.squareup.moshi.Json
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
    var achievements: List<Achievement>? = listOf()

    fun getFullLogoUrl(): String {
        return "http://media.steampowered.com/steamcommunity/public/images/apps/$appId/$logoUrl.jpg"
    }

    fun getRecentPlaytimeString(context: Context? = null) = recentPlayTime.toHours(context)

    fun getTotalPlayTimeString(context: Context? = null) = playTime.toHours(context)

    fun hasAchievements() = achievements?.isNotEmpty() ?: false

    private fun getAmountOfAchievements() = achievements?.size?.toLong() ?: 0L

    fun getAchievementsText() = when {
        hasCompletedAchievements() -> "${getCompletedAchievements().size}/${achievements?.size} (${getPercentageCompleted()}%) achievements."
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

    fun isCompleted() = getPercentageCompleted() >= 100L

    fun shouldUpdate(): Boolean {
        return Date(lastUpdated).getDaysFromNow() >= 14
    }
}