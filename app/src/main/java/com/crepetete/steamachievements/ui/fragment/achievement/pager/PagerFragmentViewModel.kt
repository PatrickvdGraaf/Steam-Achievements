package com.crepetete.steamachievements.ui.fragment.achievement.pager

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.crepetete.steamachievements.data.repository.achievement.AchievementsRepository
import com.crepetete.steamachievements.model.Achievement
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