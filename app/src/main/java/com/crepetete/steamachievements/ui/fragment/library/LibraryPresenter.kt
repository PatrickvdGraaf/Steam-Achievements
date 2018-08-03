package com.crepetete.steamachievements.ui.fragment.library

import android.widget.ImageView
import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.ui.view.game.adapter.GamesAdapter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject


class LibraryPresenter(libraryView: LibraryView) : BasePresenter<LibraryView>(libraryView),
        GamesAdapter.Listener {

    @Inject
    lateinit var gamesRepository: GamesRepository

    @Inject
    lateinit var achievementsRepository: AchievementRepository

    override fun onViewCreated() {
        getGamesFromDatabase()
    }

    private fun getGamesFromDatabase() {
        disposable.add(gamesRepository.getGameIds()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    if (it.isEmpty()){
                        getGamesFromApi()
                    } else {
                        it.forEach {
                            disposable.add(gamesRepository.getGame(it)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ game ->
                                        view.addGame(game)

                                        getAchievementsFromDb(game.appId)

                                    }, {
                                        Timber.e(it)
                                        view.showError("Error while loading games.")
                                    }))
                            getGamesFromApi()
                        }
                    }
                }, {
                    Timber.e(it)
                }))
    }

    internal fun getGamesFromApi() {
        view.showLoading()
        disposable.add(gamesRepository.getGamesFromApi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.updateGames(it)
                    view.hideLoading()
                }, {
                    Timber.e(it)
                    view.showError("Error while loading games.")
                }))
    }

    private fun insertGame(game: Game){
        gamesRepository.insert(game)
    }

    private fun getAchievementsFromDb(appId: String) {
        disposable.add(achievementsRepository.getAchievementsFromDb(appId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.updateAchievementsForGame(appId, it)
                }, {
                    Timber.e(it)
                }))
    }

    private fun getAchievementsForGame(appId: String) {
        disposable.add(achievementsRepository.getAchievementsFromApi(appId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.updateAchievementsForGame(appId, it)
                    updateAchievements(appId, it)
                }, {
                    Timber.e(it)
                }))
    }

    private fun updateAchievements(appId: String, achievements: List<Achievement>) {
        disposable.add(Single
                .fromCallable {
                    achievementsRepository.insertAchievementsIntoDb(achievements,
                            appId)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe())
    }

    override fun onGameSelected(game: Game, imageView: ImageView) {
        view.showGameActivity(game.appId, imageView)
    }

    override fun updateGame(game: Game) {
        gamesRepository.updateGame(game)
    }

    override fun addDisposable(task: Disposable) {
        disposable.add(task)
    }
}