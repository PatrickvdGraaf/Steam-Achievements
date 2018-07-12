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
    override fun getGameIds(): Single<List<String>> {
        return dao.getGameIds()
    }

    override fun getGamesFromDb(): Single<List<Game>> {
        return dao.getGamesForUser()
    }

    override fun getGamesFromApi(): Single<List<Game>> {
        val userId = userRepository.getUserId()
        return api.getGamesForUser(userId)
                .map {
                    it.response.games
                }.flatMap {
                    getAchievementsForGames(it)
                }
                .doAfterSuccess {
                    insertOrUpdateGames(it, userId)
                }
    }

    private fun getAchievementsForGames(games: List<Game>): Single<List<Game>> {
        val gameIds = games.map { game -> game.appId }

        return achievementsRepository.getAchievementsFromApi(gameIds)
                .map { achievements ->
                    // Add achievements to the games in the list.
                    if (achievements.isNotEmpty()) {
                        games.forEach { game ->
                            game.addAchievements(achievements.filter { it.appId == game.appId })
                        }
                    }
                    games
                }.map {
                    // Let other classes know that we tried to set the achievements (applies to
                    // games without achievements, clears loading status for ViewHolders for
                    // example.
                    games.forEach {
                        if (!it.achievementsWereAdded()) {
                            it.setAchievementsAdded()
                        }
                        it.userId = userRepository.getUserId()
                    }
                    games
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

    override fun getGame(appId: String): Single<Game> {
        return dao.getGame(appId)
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

    fun insertGame(game: Game) {
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