package com.crepetete.steamachievements.data.repository.achievement

import android.content.Context
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.response.achievement.AchievedAchievementResponse
import com.crepetete.steamachievements.data.api.response.achievement.DataClass
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.repository.user.UserRepository
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.utils.isConnectedToInternet
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AchievementDataSource @Inject constructor(private val context: Context,
                                                private val api: SteamApiService,
                                                private val dao: AchievementsDao,
                                                private val userRepository: UserRepository)
    : AchievementRepository {
    override fun getAchievements(appId: String): Observable<List<Achievement>> {
        return if (context.isConnectedToInternet()) {
            Observable.concatArray(getAchievementsFromDb(appId), getAchievementsFromApi(listOf(appId)))
        } else {
            getAchievementsFromDb(appId)
        }
    }

    private fun getAchievementsFromDb(appId: String) = dao.getAchievementsForGame(appId).toObservable()

    override fun getAchievementsFromApi(appIds: List<String>): Observable<List<Achievement>> {
        val dbRequests = mutableListOf<Observable<List<Achievement>>>()
        appIds.forEach {
            dbRequests.add(getAchievementsFromDb(it))
        }

        return if (context.isConnectedToInternet()) {
            val apiRequests = mutableListOf<Observable<List<Achievement>>>()
            appIds.forEach {
                apiRequests.add(getAchievementsFromApi(it).toObservable())
            }
            Observable.concatArray(Observable.merge(dbRequests), Observable.merge(apiRequests))
        } else {
            Observable.merge(dbRequests)
        }

    }

    private fun getAchievementsFromApi(appId: String): Single<List<Achievement>> {
        return api.getSchemaForGame(appId).map { response ->
            response.game.availableGameStats?.achievements ?: listOf()
        }.map {
            it.map { achievement -> achievement.appId = appId }
            it
        }.flatMap {
            getAchievedStatusForAchievementsForGame(appId, it)
        }
    }

    override fun getAchievedStatusForAchievementsForGame(appId: String,
                                                         allAchievements: List<Achievement>): Single<List<Achievement>> {
        return api.getAchievementsForPlayer(appId, userRepository.getUserId())
                .onErrorReturn {
                    AchievedAchievementResponse(DataClass())
                }
                .map {
                    if (it.playerStats.success) {
                        val ownedAchievements = it.playerStats.achievements
                        ownedAchievements.map { ownedAchievement ->
                            allAchievements.filter { it.name == ownedAchievement.apiName }
                                    .forEach {
                                        it.unlockTime = ownedAchievement.unlockTime
                                        it.achieved = ownedAchievement.achieved != 0
                                    }
                        }
                        allAchievements
                    } else {
                        listOf()
                    }
                }
    }

    override fun insertAchievementsIntoDb(achievements: List<Achievement>, appId: String) {
        if (achievements.isEmpty()) {
            return
        }

        achievements.forEach {
            it.appId = appId
            it.updatedAt = Calendar.getInstance().time
        }
        dao.insert(achievements)
    }

    override fun updateAchievementIntoDb(achievement: List<Achievement>) {
        Single.fromCallable { dao.update(achievement) }
    }

    override fun getAllAchievements(): Single<List<Achievement>> {
        return dao.getAchievements()
    }
}