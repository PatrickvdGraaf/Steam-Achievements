package com.crepetete.steamachievements.ui.activity.main.fragment.library

import com.crepetete.steamachievements.ui.common.enums.SortingType

interface NavBarInteractionListener {
    fun onSearchQueryUpdate(query: String)
    fun onSortingMethodChanged(sortingMethod: SortingType)
}