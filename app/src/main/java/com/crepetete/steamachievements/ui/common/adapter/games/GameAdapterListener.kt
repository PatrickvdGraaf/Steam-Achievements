package com.crepetete.steamachievements.ui.common.adapter.games

import android.widget.ImageView
import com.crepetete.steamachievements.vo.GameWithAchievements

/**
 * Listener for handling interaction with the [GamesAdapter].
 */
interface GameAdapterListener {
    fun onGameSelected(game: GameWithAchievements, imageView: ImageView)
    fun updateGame(game: GameWithAchievements)
}