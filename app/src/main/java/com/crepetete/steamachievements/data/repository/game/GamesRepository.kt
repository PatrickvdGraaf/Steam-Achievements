package com.crepetete.steamachievements.data.repository.game

import com.crepetete.steamachievements.model.Game
import io.reactivex.Single

interface GamesRepository {
    // Database
    fun updateGame(game: Game)
    fun getGameIds(): Single<List<String>>
    fun getGameFromDb(appId: String): Single<Game>
    fun getGamesFromApi(): Single<List<Game>>
    fun getGamesFromDb(): Single<List<Game>>
    fun insert(game: Game)
    fun insert(games: List<Game>)
}