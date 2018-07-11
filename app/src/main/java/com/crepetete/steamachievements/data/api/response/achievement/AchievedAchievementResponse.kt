package com.crepetete.steamachievements.data.api.response.achievement

import com.squareup.moshi.Json

data class AchievedAchievementResponse(@Json(name = "playerstats") val playerStats: DataClass)

data class DataClass(val steamID: String,
                     val gamaName: String,
                     val achievements: List<AchievedAchievement> = listOf(),
                     val success: Boolean)

data class AchievedAchievement(
        @Json(name = "apiname")
        val apiName: String,
        val achieved: Int,
        @Json(name = "unlockTime")
        val unlockTime: Int,
        val name: String?,
        val description: String?
)