package com.crepetete.steamachievements.api.response.achievement

data class GlobalAchievResponse(val achievementpercentages: AllAchievResponse)

data class AllAchievResponse(val achievements: List<GlobalAchievStatResponse>)

data class GlobalAchievStatResponse(
    val name: String,
    val percent: Float
)