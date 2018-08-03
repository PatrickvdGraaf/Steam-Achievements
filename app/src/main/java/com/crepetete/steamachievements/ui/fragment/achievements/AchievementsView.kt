package com.crepetete.steamachievements.ui.fragment.achievements

import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.model.Achievement

interface AchievementsView : BaseView {
    fun setTotalAchievementsInfo(achievementCount: Int)
    fun setCompletionPercentage(percentage: Double)
    fun showLatestAchievements(achievements: List<Achievement>)
    fun showBestDay(day: Pair<String, Int>)
}