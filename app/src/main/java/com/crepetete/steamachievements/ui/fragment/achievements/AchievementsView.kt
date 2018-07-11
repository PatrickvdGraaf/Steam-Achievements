package com.crepetete.steamachievements.ui.fragment.achievements

import com.crepetete.steamachievements.base.BaseView

interface AchievementsView : BaseView {
    fun setTotalAchievementsInfo(achievementCount: Int)
    fun setCompletionPercentage(percentage: Double)
}