package com.crepetete.steamachievements.ui.activity.main

import androidx.fragment.app.Fragment
import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game

interface MainView : BaseView {
    fun showPlayerDetails(persona: String)
    fun showGames(games: List<Game>)
    fun showAchievements(achievements: List<Achievement>, appId: String)
    fun openLoginActivity()
    fun setTitle(title: String)
    fun getCurrentFragment(): Fragment?
}