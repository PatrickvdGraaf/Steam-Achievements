package com.crepetete.steamachievements.data.repository.user

import android.content.SharedPreferences
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.database.dao.PlayerDao
import com.crepetete.steamachievements.model.Player
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class UserDataSource @Inject constructor(private val sharedPreferences: SharedPreferences,
                                         private val api: SteamApiService,
                                         private val dao: PlayerDao)
    : UserRepository {
    private val userIdKey = "userId"

    override fun getUserId(): String = sharedPreferences.getString(userIdKey, getInvalidId())

    override fun putUserId(userId: String) {
        sharedPreferences.edit().putString(userIdKey, userId).apply()
    }

    override fun getPlayer(userId: String): Observable<Player> {
        val observable = Observable.concat(getPlayerFromDb(userId), getPlayerFromApi(userId))
        return observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getPlayerFromApi(id: String): Observable<Player> {
        return api.getUserInfo(id)
                .toObservable()
                .map { it.response.players[0] }
                .doOnNext { addPlayerToDb(it) }
    }

    private fun getPlayerFromDb(id: String): Observable<Player> {
        return dao.getPlayerById(id)
                .filter { it.isNotEmpty() }
                .map { it[0] }
                .toObservable()
    }

    private fun addPlayerToDb(player: Player) {
        Single.fromCallable { dao.insert(player) }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    Timber.d("Added ${player.persona} to Database.")
                }, {
                    Timber.e(it)
                })
    }

    override fun getPlayerName(userId: String): Single<String> {
        return dao.getPlayerName(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getCurrentPlayer(): Single<Player?> {
        return dao.getPlayerById(getUserId())
                .map { it[0] }
    }
}