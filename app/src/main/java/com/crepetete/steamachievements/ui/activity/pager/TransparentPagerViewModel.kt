package com.crepetete.steamachievements.ui.activity.pager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

/**
 * ViewModel for [TransparentPagerActivity], responsible for holding the AchievementsPager data in
 * Pairs (getName, getAppId).
 */
class TransparentPagerViewModel @Inject constructor() : ViewModel() {
    private val _index = MutableLiveData<Int>()
    val index: LiveData<Int>
        get() = _index

    private val _achievementData = MutableLiveData<List<Pair<String, String>>>()
    val achievementData: LiveData<List<Pair<String, String>>>
        get() = _achievementData

    fun setIndex(index: Int) {
        if (_index.value == index) {
            return
        }
        _index.value = index
    }

    fun setAchievementData(names: List<String>, appIds: List<String>) {
        _achievementData.value = names.mapIndexed { index, s ->
            Pair(s, appIds[index])
        }
    }
}