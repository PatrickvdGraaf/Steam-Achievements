package com.crepetete.steamachievements.data.repository.user

import com.crepetete.steamachievements.model.Player
import io.reactivex.Observable
import io.reactivex.Single

interface UserRepository {
    fun getInvalidId(): String = "-1"
    fun getUserId(): String
    fun putUserId(userId: String)
    fun getPlayer(userId: String): Observable<Player>
    fun getPlayerName(userId: String): Single<String>
    fun getCurrentPlayer(): Single<Player?>
}