package com.crepetete.steamachievements.ui.fragment.library

interface NavbarInteractionListener {
    fun onSearchQueryUpdate(query: String)
    fun onSortingMethodChanged(sortingMethod: Int)
}