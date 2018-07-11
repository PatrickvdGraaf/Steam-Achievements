package com.crepetete.steamachievements.ui.activity.game

import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.base.BaseView

interface GameView : BaseView {
    fun setGameInfo(game: Game)
}