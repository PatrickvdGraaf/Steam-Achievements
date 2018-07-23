package com.crepetete.steamachievements.ui.fragment.library

import android.widget.ImageView
import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game

interface LibraryView : BaseView {
    fun updateGames(games: List<Game>)
    fun showGameActivity(appId: String, imageView: ImageView)
    fun updateAchievementsForGame(appId: String, achievements: List<Achievement>)
}