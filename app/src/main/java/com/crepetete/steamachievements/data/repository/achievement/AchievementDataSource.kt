package com.crepetete.steamachievements.data.repository.achievement

import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.response.achievement.AchievedAchievementResponse
import com.crepetete.steamachievements.data.api.response.achievement.DataClass
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.repository.user.UserRepository
import com.crepetete.steamachievements.model.Achievement
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class AchievementDataSource @Inject constructor(private val api: SteamApiService,
                                                private val dao: AchievementsDao,
                                                private val userRepository: UserRepository)
    : AchievementRepository {
    override fun getAchievementsFromDb(appIds: List<String>): Single<List<Achievement>> {
        val tasks = mutableListOf<Observable<List<Achievement>>>()
        appIds.forEach {
            tasks.add(getAchievementsFromDb(it).toObservable())
        }

        return Observable.fromIterable(tasks)
                .flatMap {
                    it.observeOn(Schedulers.computation())
                }
                .toList()
                .map {
                    val allAchievements = mutableListOf<Achievement>()
                    it.forEach { list ->
                        allAchievements.addAll(list)
                    }
                    allAchievements
                }
    }

    override fun getAchievementsFromDb(appId: String) = dao.getAchievementsForGame(appId)

    override fun getAchievementsFromApi(appIds: List<String>): Single<List<Achievement>> {
        val tasks = mutableListOf<Observable<List<Achievement>>>()
        appIds.forEach {
            tasks.add(getAchievementsFromApi(it).toObservable())
        }

        return Observable.fromIterable(tasks)
                .flatMap {
                    it.observeOn(Schedulers.computation())
                }
                .toList()
                .map {
                    val allAchievements = mutableListOf<Achievement>()
                    it.forEach { list ->
                        allAchievements.addAll(list)
                    }
                    allAchievements
                }
                .map { achievements ->
                    achievements.forEach { it.updatedAt = Calendar.getInstance().time }
                    achievements
                }
    }

    override fun getAchievementsFromApi(appId: String): Single<List<Achievement>> {
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
                                .filter { it.achieved != 0 }
                        ownedAchievements.map { ownedAchievement ->
                            allAchievements.filter {
                                it.name == ownedAchievement.apiName
                            }.forEach {
                                it.unlockTime = ownedAchievement.getUnlockDate()
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
        dao.insert(achievements)
    }

    override fun updateAchievementIntoDb(achievement: List<Achievement>) {
        Single.fromCallable { dao.update(achievement) }
    }

    override fun getAllAchievements(): Single<List<Achievement>> {
        return dao.getAchievements()
    }
}