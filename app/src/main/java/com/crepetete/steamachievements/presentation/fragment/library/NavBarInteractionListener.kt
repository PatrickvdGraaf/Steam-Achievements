package com.crepetete.steamachievements.presentation.fragment.library

import com.crepetete.steamachievements.presentation.common.enums.SortingType

interface NavBarInteractionListener {
    fun onSearchQueryUpdate(query: String)
    fun onSortingMethodChanged(sortingMethod: SortingType)
}