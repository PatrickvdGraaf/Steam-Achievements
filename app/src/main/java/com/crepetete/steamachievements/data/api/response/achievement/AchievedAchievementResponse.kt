package com.crepetete.steamachievements.data.api.response.achievement

import com.squareup.moshi.Json
import java.util.Date

data class AchievedAchievementResponse(@field:Json(name = "playerstats") val playerStats: DataClass?)

data class DataClass(
    val steamID: String = "-1",
    val gameName: String = "",
    val achievements: List<AchievedAchievement> = listOf(),
    val success: Boolean = false
)

data class AchievedAchievement(
    @field:Json(name = "apiname")
    val apiName: String,
    val achieved: Int,
    @field:Json(name = "unlocktime")
    val unlockTime: Long,
    val name: String?,
    val description: String?
) {
    fun getUnlockDate(): Date {
        return Date(unlockTime * 1000L)
    }
}