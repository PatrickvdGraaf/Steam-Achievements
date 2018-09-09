package com.crepetete.steamachievements.ui.fragment.library

import android.widget.ImageView
import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.ui.view.game.adapter.GamesAdapter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class LibraryPresenter(libraryView: LibraryView,
                       private val gamesRepository: GamesRepository,
                       private val achievementsRepository: AchievementRepository)
    : BasePresenter<LibraryView>(libraryView),
        GamesAdapter.Listener {
    /**
     * When the view is created, we first retreive all games from the Database. This call will
     * automatically update the games via an API call when all or zero games we're retrieved.
     */
    override fun onViewCreated() {
        getGamesFromDb()
    }

    /**
     * Retrieves a list of all Game's appIds, which will be used to make a getGameFromDb call for each id
     * in the list, unless the list is empty. Then if will skip to retrieving all games from the API
     * for an update.
     *
     * TODO maybe not update each time this message it called, maybe use a timestamp?
     */
    fun getGameIdsFromDb() {
        disposable.add(gamesRepository.getGameIds()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ gameIds ->
                    if (gameIds.isEmpty()) {
                        // No games available, get data from API.
                        showDebugToast("No games in database.")
                        getGamesFromApi()
                    } else {
                        showDebugToast("Retrieved ${gameIds.size} game Ids from database, starting getGameFromDb")
                        gameIds.forEach { appId ->
                            getGameFromDb(appId)
                        }
                        view.hideLoading()

                        // Update
                        getGamesFromApi()
                    }
                }, {
                    onError(it)
                }))
    }

    /**
     * Retrieves a single game from the database.
     *
     * @param appId ID of the requested Game.
     */
    private fun getGameFromDb(appId: String) {
        disposable.add(gamesRepository.getGameFromDb(appId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ game ->
                    view.addGame(game)
                }, {
                    Timber.e(it)
                }))
    }

    /**
     * Retrieves a list of all owned games from the API.
     * When there are results it updates the View with the new information.
     *
     * TODO maybe not update each time this message it called, maybe use a timestamp?
     */
    private fun getGamesFromDb() {
        showDebugToast("Calling getGames from Db")
        disposable.add(gamesRepository.getGamesFromDb()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.updateGames(it)
                    view.hideLoading()

                    showDebugToast("Retrieved ${it.size} games from Db, starting  starting getGameFromApi")
                    getGamesFromApi()
                }, {
                    onError(it)
                }))
    }

    /**
     * Retrieves a list of all owned games from the API.
     * When there are results it updates the View with the new information.
     */
    private fun getGamesFromApi() {
        showDebugToast("Calling getGames from API")
        disposable.add(gamesRepository.getGamesFromApi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.updateGames(it)
                    view.hideLoading()

                    showDebugToast("Retrieved ${it.size} games from API, starting db update.")
                    insertGamesInDb(it)
                }, {
                    onError(it)
                }))
    }

    private fun onError(throwable: Throwable? = null,
                        message: String = "Error while loading games.") {
        Timber.e(throwable)
        view.showError(message)
    }

    private fun insertGame(game: Game) {
        gamesRepository.insert(game)
    }

    private fun getAchievementsFromDb(appId: String) {
        disposable.add(achievementsRepository.getAchievementsFromDb(appId)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showDebugToast("Inserted game in the database.")
                }, {
                    onError(it, "Error while getting achievements from DB")
                }))
    }

    /**
     * Inserts a single game into the Room database.
     */
    private fun insertGameInDb(game: Game) {
        disposable.add(Single.fromCallable { gamesRepository.insert(game) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showDebugToast("Inserted game in the database.")
                }, {
                    onError(it, "Error while getting achievements from DB")
                }))
    }

    /**
     * Inserts a list of games into the Room database.
     */
    private fun insertGamesInDb(games: List<Game>) {
        disposable.add(Single.fromCallable { gamesRepository.insert(games) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showDebugToast("Inserted games in the database.")
                }, {
                    onError(it, "Error while getting achievements from DB")
                }))
    }

    /**
     * Listener method for the [GamesAdapter.Listener]. Makes the view open a new [GameActivity] for
     * the selected [Game].
     */
    override fun onGameSelected(game: Game, imageView: ImageView) {
        view.showGameActivity(game.appId, imageView)
    }

    override fun updateGame(game: Game) {
//        gamesRepository.updateGame(game)
    }

    override fun addDisposable(task: Disposable) {
        disposable.add(task)
    }
}