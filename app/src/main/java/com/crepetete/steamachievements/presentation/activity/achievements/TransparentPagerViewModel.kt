package com.crepetete.steamachievements.presentation.activity.achievements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.domain.model.Achievement
import javax.inject.Inject

/**
 * ViewModel for [TransparentPagerActivity], responsible for holding the AchievementsPager data.
 */
class TransparentPagerViewModel @Inject constructor() : ViewModel() {
    private val _index = MutableLiveData<Int>()
    val index: LiveData<Int>
        get() = _index

    private val _achievementData = MutableLiveData<List<Achievement>>()
    val achievementData: LiveData<List<Achievement>>
        get() = _achievementData

    fun setIndex(index: Int) {
        if (_index.value == index) {
            return
        }
        _index.value = index
    }

    fun setAchievementData(achievements: List<Achievement>?) {
        _achievementData.value = achievements
    }
}