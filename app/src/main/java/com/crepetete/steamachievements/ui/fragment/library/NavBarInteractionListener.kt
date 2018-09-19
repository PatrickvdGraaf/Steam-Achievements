package com.crepetete.steamachievements.ui.fragment.library

import com.crepetete.steamachievements.ui.common.adapter.games.SortingType

interface NavBarInteractionListener {
    fun onSearchQueryUpdate(query: String)
    fun onSortingMethodChanged(sortingMethod: SortingType)
}