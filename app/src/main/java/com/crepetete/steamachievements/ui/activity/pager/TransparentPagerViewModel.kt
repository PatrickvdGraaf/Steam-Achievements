package com.crepetete.steamachievements.ui.activity.pager

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import javax.inject.Inject

class TransparentPagerViewModel @Inject constructor() : ViewModel() {
    private val _index = MutableLiveData<Int>()
    val index: LiveData<Int>
        get() = _index

    private val _achievementNames = MutableLiveData<List<String>>()
    val achievementNames: LiveData<List<String>>
        get() = _achievementNames

    fun setIndex(index: Int) {
        if (_index.value == index) {
            return
        }
        _index.value = index
    }

    fun setAchievementNames(names: List<String>) {
        _achievementNames.value = names
    }
}