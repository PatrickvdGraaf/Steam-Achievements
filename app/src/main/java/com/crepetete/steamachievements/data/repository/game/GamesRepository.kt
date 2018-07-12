package com.crepetete.steamachievements.data.repository.game

import com.crepetete.steamachievements.model.Game
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

interface GamesRepository {
    // Database
    fun updateGame(game: Game)
    fun getGameIds(): Single<List<String>>
    fun getGame(appId: String): Single<Game>
    fun getGamesFromApi(): Single<List<Game>>
    fun getGamesFromDb(): Single<List<Game>>
}