package com.crepetete.steamachievements.ui.activity.game

import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class GamePresenter(gameView: GameView, private val gameId: String) : BasePresenter<GameView>(gameView) {
    private var disposable: CompositeDisposable = CompositeDisposable()

    @Inject
    lateinit var gamesRepository: GamesRepository

    override fun onViewCreated() {
        getGame()
    }

    private fun getGame() {
//        view.showLoading()
        disposable.add(gamesRepository.getGame(gameId)
                .subscribe({
                    view.setGameInfo(it)
                }, {
                    Timber.e(it)
                }))
    }

    override fun onViewDestroyed() {
        disposable.dispose()
    }
}