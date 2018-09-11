package com.crepetete.steamachievements.ui.common.graph.point

import java.util.*

/**
 * Simple OnTapListener that provides feedback from the
 * [com.crepetete.steamachievements.ui.common.graph.AchievementsGraphViewUtil] after the graph is
 * drawn.
 */
interface OnGraphDateTappedListener {
    fun onDateTapped(date: Date)
}