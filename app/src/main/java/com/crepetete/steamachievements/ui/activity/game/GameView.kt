package com.crepetete.steamachievements.ui.activity.game

import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game

interface GameView : BaseView {
    fun setGameInfo(game: Game?)
    fun setAchievements(achievements: List<Achievement>)
}