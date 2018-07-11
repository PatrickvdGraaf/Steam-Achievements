package com.crepetete.steamachievements.data.repository.game

import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.user.UserRepository
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
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

    override fun getGames(): Observable<List<Game>> {
        return Observable.concatArray(getGamesFromDb(), getGamesFromApi())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getGamesFromDb(): Observable<List<Game>> {
        return dao.getGamesForUser()
                .toObservable()
    }

    override fun getGamesFromApi(): Observable<List<Game>> {
        val userId = userRepository.getUserId()
        return api.getGamesForUser(userId)
                .map {
                    it.response.games
                }.flatMap {
                    getAchievementsForGames(it)
                }.doOnNext {
                    insertOrUpdateGames(it, userId)
                }
    }

    private fun getAchievementsForGames(games: List<Game>): Observable<List<Game>> {
        val gameIds = games.map { game -> game.appId }
        return achievementsRepository.getAchievementsFromApi(gameIds)
                .flatMap { achievements ->
                    Observable.fromCallable { addAchievementsToGames(games, achievements) }
                }
    }

    private fun addAchievementsToGames(games: List<Game>, achievements: List<Achievement>): List<Game> {
        games.map {
            it.achievements = achievements.filter { achievement -> achievement.appId == it.appId }
        }
        return games
    }

    private fun insertOrUpdateGames(games: List<Game>, userId: String) {
        getGameIds()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ ids ->
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { it[0] }
    }

    override fun updateGame(game: Game) {
        Single.fromCallable { dao.update(listOf(game)) }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    Timber.d("Updated ${game.name} in the Database.")
                }, {
                    Timber.e(it)
                })
    }

    fun insertGame(game: Game) {
        Single.fromCallable { dao.insert(listOf(game)) }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    Timber.d("Updated ${game.name} in the Database.")
                }, {
                    Timber.e(it)
                })
    }
}