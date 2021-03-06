package com.crepetete.steamachievements.ui.activity.game.fragment.achievement.pager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.vo.Achievement
import javax.inject.Inject

/**
 * ViewModel for an [AchievementPagerFragment]. Holds an Achievement object for the view to present.
 */
class PagerFragmentViewModel @Inject constructor() : ViewModel() {

    private val achievement = MutableLiveData<Achievement>()

    fun setAchievementInfo(newAchievement: Achievement) {
        achievement.value = newAchievement
    }

    fun getAchievement(): LiveData<Achievement> = achievement
}