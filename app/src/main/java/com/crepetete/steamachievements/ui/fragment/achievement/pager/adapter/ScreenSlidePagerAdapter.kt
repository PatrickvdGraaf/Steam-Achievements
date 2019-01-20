package com.crepetete.steamachievements.ui.fragment.achievement.pager.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.crepetete.steamachievements.ui.fragment.achievement.pager.AchievementPagerFragment
import com.crepetete.steamachievements.vo.Achievement

/**
 * Adapter for Horizontal Achievements ViewPager.
 *
 * Creates [AchievementPagerFragment]s for each Achievement.
 */
class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    private var achievementData: List<Achievement> = listOf()

    override fun getItem(position: Int) = AchievementPagerFragment.getInstance(achievementData[position])

    override fun getCount() = achievementData.size

    fun updateAchievements(data: List<Achievement>?) {
        //        val diffResult = DiffUtil.calculateDiff(AchievementDiffCallback(achievementData, data))
        //        diffResult.dispatchUpdatesTo(this)

        if (data != null) {
            achievementData = data
            notifyDataSetChanged()
        }
    }
}