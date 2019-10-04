package com.crepetete.steamachievements.ui.activity.game.fragment.achievement.pager.adapter

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.crepetete.steamachievements.ui.activity.game.fragment.achievement.pager.AchievementPagerFragment
import com.crepetete.steamachievements.vo.Achievement

/**
 * Adapter for Horizontal Achievements ViewPager.
 *
 * Creates [AchievementPagerFragment]s for each Achievement.
 */
class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
    private var achievementData: List<Achievement> = listOf()

    /**
     * Called when the host view is attempting to determine if an item’s position has changed.
     * Returns [PagerAdapter.POSITION_UNCHANGED] if the position of the given item has not changed
     * or [PagerAdapter.POSITION_NONE] if the item is no longer present in the adapter.
     * The default implementation assumes that items will never change position and always
     * returns [PagerAdapter.POSITION_NONE].
     *
     * Always returning POSITION_NONE is memory and performance inefficient.
     * It will always detach the current visible fragments and recreate them even if their position in the dataset hasn’t changed.
     */
    override fun getItemPosition(`object`: Any): Int {
        return if (achievementData.contains(`object`)) {
            achievementData.indexOf(`object`)
        } else {
            PagerAdapter.POSITION_NONE
        }
    }

    override fun getItem(position: Int) = AchievementPagerFragment.getInstance(achievementData[position])

    override fun getCount() = achievementData.size

    fun updateAchievements(list: List<Achievement>?) {
        achievementData = list ?: listOf()
        notifyDataSetChanged()
    }
}