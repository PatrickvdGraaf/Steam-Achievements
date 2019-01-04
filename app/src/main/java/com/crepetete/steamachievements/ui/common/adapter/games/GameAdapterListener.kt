package com.crepetete.steamachievements.ui.common.adapter.games

import android.widget.ImageView
import com.crepetete.steamachievements.vo.Game
import io.reactivex.disposables.Disposable

/**
 * Listener for handling interaction with the [GamesAdapter].
 */
interface GameAdapterListener {
    fun onGameSelected(game: Game, imageView: ImageView)
    fun updateGame(game: Game)
    fun addDisposable(task: Disposable)
}