package com.crepetete.steamachievements.ui.fragment.achievement.pager.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.crepetete.steamachievements.ui.fragment.achievement.pager.AchievementPagerFragment

/**
 * Adapter for Horizontal Achievements ViewPager.
 *
 * Creates [AchievementPagerFragment]s for each Achievement.
 */
class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    private var achievementData: List<Pair<String, String>> = listOf()

    override fun getItem(position: Int): Fragment {
        val fragment = AchievementPagerFragment()
        val bundle = Bundle()
        bundle.putString(AchievementPagerFragment.INTENT_KEY_NAME, achievementData[position].first)
        bundle.putString(AchievementPagerFragment.INTENT_KEY_APP_ID, achievementData[position].second)
        fragment.arguments = bundle
        return fragment
    }

    override fun getCount() = achievementData.size

    fun updateAchievements(data: List<Pair<String, String>>) {
        achievementData = data
        notifyDataSetChanged()
    }
}