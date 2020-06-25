package com.crepetete.steamachievements.presentation.fragment.achievement.pager.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.presentation.fragment.achievement.pager.AchievementPagerFragment

/**
 * Adapter for Horizontal Achievements ViewPager.
 *
 * Creates [AchievementPagerFragment]s for each Achievement.
 */
class ScreenSlidePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    private var achievementData: List<Achievement> = listOf()

    fun updateAchievements(list: List<Achievement>?) {
        achievementData = list ?: listOf()
        notifyDataSetChanged()
    }

    override fun getItemCount() = achievementData.size

    override fun createFragment(position: Int): Fragment {
        return AchievementPagerFragment.getInstance(achievementData[position])
    }
}