package com.crepetete.steamachievements.data.repository.game

import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.user.UserRepository
import com.crepetete.steamachievements.model.Game
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class GamesDataSource @Inject constructor(private val api: SteamApiService,
                                          private val dao: GamesDao,
                                          private val userRepository: UserRepository,
                                          private val achievementsRepository: AchievementRepository)
    : GamesRepository {


    /**
     * Retrieves a list of all Game IDs in the database.
     */
    override fun getGameIds(): Single<List<String>> {
        return dao.getGameIds()
    }

    /**
     * Retrieves a list of all Games in the database.
     */
    override fun getGamesFromDb(): Single<List<Game>> {
        return dao.getGamesForUser()
    }

    /**
     * Inserts a single Game in the database.
     */
    override fun insert(game: Game) {
        dao.insert(game)
    }

    /**
     * Inserts a list of Games into the database.
     */
    override fun insert(games: List<Game>) {
        dao.insert(games)
    }

    /**
     * Retrieves all owned games from API.
     * It also retrieves the Achievements for each games from the API before the list is returned.
     * This is because Achievements are retrieved in a separate call.
     */
    override fun getGamesFromApi(): Single<List<Game>> {
        val userId = userRepository.getUserId()
        return api.getGamesForUser(userId)
                .map {
                    it.response.games
                }
                .flatMap {
                    addAchievementsToGamesFromApi(it)
                }
                .doAfterSuccess {
                    insertOrUpdateGames(it, userId)
                }
    }

    private fun addAchievementsToGamesFromApi(games: List<Game>): Single<List<Game>> {
        val gameIds = games.map { game -> game.appId }

        return achievementsRepository.getAchievementsFromApi(gameIds).map { achievements ->
            // Add achievements to the games in the list.
            if (achievements.isNotEmpty()) {
                games.forEach { game ->
                    val achievementsForGame = achievements.filter {
                        it.appId == game.appId
                    }
                    game.addAchievements(achievementsForGame)
                    achievementsRepository.insertAchievementsIntoDb(achievementsForGame, game.appId)
                }
            }
            games
        }.map { updatedGames ->
            // Let other classes know that we tried to set the achievements (applies to
            // games without achievements, clears loading status for ViewHolders for
            // example.
            updatedGames.forEach {
                if (!it.achievementsWereAdded()) {
                    it.setAchievementsAdded()
                }
                it.userId = userRepository.getUserId()
            }
            updatedGames
        }
    }

    private fun insertOrUpdateGames(games: List<Game>, userId: String) {
        getGameIds().subscribe({ ids ->
            val newGames = games
                    .filter { !ids.contains(it.appId) }
                    .map {
                        it.lastUpdated = Calendar.getInstance().time.time
                        it
                    }
            if (newGames.isNotEmpty()) {
                newGames.map {
                    it.userId = userId
                }
                newGames.forEach {
                    insertGame(it)
                }
            }

            val updatedGames = games
                    .filter {
                        ids.contains(it.appId) && it.shouldUpdate()
                    }.map {
                        it.lastUpdated = Calendar.getInstance().time.time
                        it.userId = userId
                        it
                    }
            if (updatedGames.isNotEmpty()) {
                updatedGames.forEach {
                    updateGame(it)
                }
            }
        }, {
            Timber.e(it)
        })
    }

    override fun getGameFromDb(appId: String): Single<Game> {
        var game: Game? = null
        return dao.getGame(appId)
                .flatMap {
                    game = it
                    achievementsRepository.getAchievementsFromDb(it.appId)
                }.map {
                    game?.setAchievements(it)
                    game
                }
    }

    override fun updateGame(game: Game) {
        Single.fromCallable { dao.update(listOf(game)) }
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .subscribe({
                    Timber.d("Updated ${game.name} in the Database.")
                }, {
                    Timber.e(it)
                })
    }

    private fun insertGame(game: Game) {
        Single.fromCallable { dao.insert(listOf(game)) }
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .subscribe({
                    Timber.d("Updated ${game.name} in the Database.")
                }, {
                    Timber.e(it)
                })
    }
}