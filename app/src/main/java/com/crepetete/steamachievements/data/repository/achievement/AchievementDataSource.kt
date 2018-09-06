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
import kotlin.collections.HashMap

class AchievementDataSource @Inject constructor(private val api: SteamApiService,
                                                private val dao: AchievementsDao,
                                                private val userRepository: UserRepository)
    : AchievementRepository {
    override fun getBestAchievementsDay(): Single<Pair<String, Int>> {
        return dao.getAchievements()
                .map { allAchievements ->
                    // Create a list of Pairs<Date, Int>> from all achievements
                    val newList = mutableListOf<Pair<String, Int>>()
                    allAchievements
                            .filter {
                                it.achieved
                            }.forEach { achievement ->
                                val key = achievement.getDateStringNoTime()
                                if (key != null) {
                                    newList.add(Pair(key, 1))
                                }
                            }
                    newList
                }.map { pairs ->
                    // Add and collect sizes for similar dates
                    val achievementsByDate = HashMap<String, Int>()
                    pairs.forEach { pair ->
                        var oldSize = achievementsByDate[pair.first]
                        oldSize = oldSize?.plus(pair.second) ?: pair.second
                        achievementsByDate[pair.first] = oldSize
                    }
                    achievementsByDate
                }.map {
                    // Get the pair with the biggest size.
                    var bestDay = Pair("No Achievements yet.", 0)
                    it.forEach { t, u ->
                        if (u > bestDay.second) {
                            bestDay = Pair(t, u)
                        }
                    }

                    bestDay
                }
    }

    private fun isNewDate(existingDate: Date, otherDate: Date): Boolean {
        val existingCalendat = Calendar.getInstance()
        existingCalendat.time = existingDate

        val otherCalendar = Calendar.getInstance()
        otherCalendar.time = otherDate
        return existingCalendat.get(Calendar.DAY_OF_MONTH) != otherCalendar.get(Calendar.DAY_OF_MONTH)
                || existingCalendat.get(Calendar.MONTH) != otherCalendar.get(Calendar.MONTH)
                || existingCalendat.get(Calendar.YEAR) != otherCalendar.get(Calendar.YEAR)
    }

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
        }.flatMap {
            getGlobalStats(appId, it)
        }.map {
            updateAchievementIntoDb(it)
            it
        }
    }

    private fun getGlobalStats(appId: String, achievements: List<Achievement>): Single<List<Achievement>> {
        return api.getGlobalAchievementStats(appId).map {
            it.achievementpercentages.achievements.map { response ->
                achievements.filter { it.name == response.name }.forEach { achievement ->
                    achievement.percentage = response.percent
                }
            }

            achievements
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

    override fun getAllAchievementsFromDb(): Single<List<Achievement>> {
        return dao.getAchievements()
    }
}