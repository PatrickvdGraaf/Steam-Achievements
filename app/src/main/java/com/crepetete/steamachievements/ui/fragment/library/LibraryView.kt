package com.crepetete.steamachievements.ui.fragment.library

import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.model.Game

interface LibraryView : BaseView {
    fun updateGames(games: List<Game>)
}