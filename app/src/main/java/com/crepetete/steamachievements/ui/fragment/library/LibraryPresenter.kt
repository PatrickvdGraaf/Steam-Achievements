package com.crepetete.steamachievements.ui.fragment.library

import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.ui.fragment.library.adapter.GamesAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class LibraryPresenter(libraryView: LibraryView) : BasePresenter<LibraryView>(libraryView),
        GamesAdapter.Listener {
    private var disposable: CompositeDisposable = CompositeDisposable()

    @Inject
    lateinit var gamesRepository: GamesRepository

    override fun onViewCreated() {
    }

    internal fun getGamesFromDatabase() {
        view.showLoading()
        disposable.add(gamesRepository.getGamesFromDb()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.hideLoading()
                    view.updateGames(it)
                }, {
                    Timber.e(it)
                    view.hideLoading()
                    view.showError("Error while updating games.")
                }))
    }

    internal fun getGamesFromApi() {
        disposable.add(gamesRepository.getGamesFromApi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.updateGames(it)
                }, {
                    Timber.e(it)
                    view.showError("Error while loading games.")
                }))
    }

    override fun onGameSelected(game: Game) {
        // TODO
    }

    override fun updateGame(game: Game) {
        gamesRepository.updateGame(game)
    }

    override fun addDisposable(task: Disposable) {
        disposable.add(task)
    }
}