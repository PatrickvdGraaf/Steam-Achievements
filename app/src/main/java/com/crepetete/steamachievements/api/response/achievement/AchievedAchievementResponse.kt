package com.crepetete.steamachievements.api.response.achievement

import com.squareup.moshi.Json
import timber.log.Timber
import java.util.*

data class AchievedAchievementResponse(@Json(name = "playerstats") val playerStats: DataClass)

data class DataClass(val steamID: String = "-1",
                     val gameName: String = "",
                     val achievements: List<AchievedAchievement> = listOf(),
                     val success: Boolean = false)

data class AchievedAchievement(
        @Json(name = "apiname")
        val apiName: String,
        val achieved: Int,
        @Json(name = "unlocktime")
        val unlockTime: Long,
        val name: String?,
        val description: String?
) {
    fun getUnlockDate(): Date {
        return Date(unlockTime * 1000L)
    }
}