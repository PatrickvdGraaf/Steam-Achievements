package com.crepetete.steamachievements.ui.fragment.achievement.pager

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

/**
 * Adapter for Horizontal Achievements ViewPager.
 *
 * Creates [AchievementPagerFragment]s for each Achievement.
 */
class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    private var achievementNames: List<String> = listOf()

    override fun getItem(position: Int): Fragment {
        val fragment = AchievementPagerFragment()
        val bundle = Bundle()
        bundle.putString(AchievementPagerFragment.INTENT_KEY_NAME, achievementNames[position])
        fragment.arguments = bundle
        return fragment
    }

    override fun getCount() = achievementNames.size

    fun updateAchievements(names: List<String>) {
        achievementNames = names
        notifyDataSetChanged()
    }
}