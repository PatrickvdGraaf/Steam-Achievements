package com.crepetete.steamachievements.ui.activity.game

import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class GamePresenter(gameView: GameView, private val gameId: String) : BasePresenter<GameView>(gameView) {
    @Inject
    lateinit var gamesRepository: GamesRepository

    override fun onViewCreated() {
        getGame()
    }

    private fun getGame() {
//        view.showLoading()
        disposable.add(gamesRepository.getGame(gameId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.setGameInfo(it)
                }, {
                    Timber.e(it)
                }))
    }
}