package com.crepetete.steamachievements.ui.activity.game

import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class GamePresenter(gameView: GameView,
                    private val gamesRepository: GamesRepository,
                    private val achievementsRepository: AchievementRepository)
    : BasePresenter<GameView>(gameView) {

    private var _gameId: String? = null

    override fun onViewCreated() {
        val id = _gameId
        if (id != null) {
            getGameFromDb(id)
            getAchievementsForGame(id)
        }
    }

    fun setGameId(gameId: String) {
        _gameId = gameId
//        getGameFromDb(gameId)
//        getAchievementsForGame(gameId)
    }

    private fun getGameFromDb(gameId: String) {
        view.showLoading()
        disposable.add(gamesRepository.getGameFromDb(gameId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.setGameInfo(it)
                    view.hideLoading()
                }, {
                    Timber.e(it)
                    view.hideLoading()
                }))
    }

    private fun getAchievementsForGame(gameId: String) {
        disposable.add(achievementsRepository.getAchievementsFromApi(listOf(gameId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.setAchievements(it)
                }, {
                    Timber.e(it)
                }))
    }
}