package com.crepetete.steamachievements.ui.fragment.achievement.pager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.vo.Achievement
import javax.inject.Inject

/**
 * ViewModel for an [AchievementPagerFragment]. Holds an Achievement object for the view to present.
 */
class PagerFragmentViewModel @Inject constructor(
        private val achievementRepository: AchievementsRepository) : ViewModel() {

    var achievements: LiveData<List<Achievement>> = MutableLiveData<List<Achievement>>()

    private val _achievement = MutableLiveData<Achievement>()
    val achievement: LiveData<Achievement>
        get() = _achievement

    fun setAchievementInfo(name: String, appId: String) {
        achievements = achievementRepository.getAchievement(name, appId)
    }
}