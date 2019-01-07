package com.crepetete.steamachievements.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.ApiResponse
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.achievement.AchievedAchievementResponse
import com.crepetete.steamachievements.api.response.achievement.GlobalAchievResponse
import com.crepetete.steamachievements.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.db.dao.AchievementsDao
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.util.RateLimiter
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Resource
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class AchievementsRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val userRepository: UserRepository,
    private val dao: AchievementsDao,
    private val api: SteamApiService
) {

    private val achievementsListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    /**
     * Fetch all emptyAchievements for a specific game without their global completion rate and achieved
     * info.
     */
    fun loadAchievementsForGame(appId: String): LiveData<Resource<List<Achievement>>> {
        return object : NetworkBoundResource<List<Achievement>, SchemaResponse>(appExecutors) {
            override fun saveCallResult(item: SchemaResponse) {
                Timber.d("steamachievements; Saving Achievements in DB for getAppId: $appId")

                val achievements = item.game.availableGameStats?.achievements
                if (achievements != null) {
                    achievements.forEach {
                        it.appId = appId
                    }
                    dao.insert(achievements)
                }
            }

            override fun shouldFetch(data: List<Achievement>?) = data == null
                || data.isEmpty()
                || achievementsListRateLimit.shouldFetch("ACHIEVEMENTS_$appId")

            override fun loadFromDb(): LiveData<List<Achievement>> {
                Timber.i("steamachievements; Getting achievements from database for game $appId")
                return dao.getAchievements(appId)
            }

            override fun createCall(): LiveData<ApiResponse<SchemaResponse>> = api.getSchemaForGame(appId)

        }.asLiveData()
    }

    @SuppressLint("CheckResult")
    fun updateAchievementsForGame(appId: String) {
        api.getSchemaForGameAsSingle(appId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val data = it.game.availableGameStats?.achievements
                if (data != null) {
                    data.forEach { achievement ->
                        achievement.appId = appId
                    }

                    getStatsPerAchievements(appId, data)
                }
            }, {
                Timber.e(it)
            })
    }

    private fun insertAchievementsList(achievements: List<Achievement>) {
        Single.create<Void> {
            dao.insert(achievements)
        }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe()
    }

    @SuppressLint("CheckResult")
    private fun getStatsPerAchievements(appId: String, allAchievements: List<Achievement>) {
        Timber.d("Getting Personal achievement stats for game: $appId")
        api.getAchievementsForPlayerAsSingle(appId, userRepository.getCurrentPlayerId() ?: "-1")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                val achievements = mutableListOf<Achievement>()
                response.playerStats.achievements
                    .asSequence()
                    .filter { it.achieved != 0 }
                    .map { ownedAchievement ->
                        allAchievements.filter {
                            it.name == ownedAchievement.apiName
                        }.forEach { it ->
                            it.unlockTime = ownedAchievement.getUnlockDate()
                            it.achieved = ownedAchievement.achieved != 0
                            if (ownedAchievement.description != null) {
                                it.description = ownedAchievement.description
                            }
                            achievements.add(it)
                        }
                    }
                    .toList()
                //                getGlobalStats(getAppId, allAchievements, listener)
                insertAchievementsList(allAchievements)
            }, {
                Timber.e(it)
                insertAchievementsList(allAchievements)
            })
    }

    @SuppressLint("CheckResult")
    private fun getGlobalStats(appId: String, allAchievements: List<Achievement>) {
        Timber.d("Getting Global achievement stats for game: $appId")
        api.getGlobalAchievementStatsAsSingle(appId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ item ->
                item.achievementpercentages.achievements.forEach { response ->
                    allAchievements.filter {
                        it.name == response.name
                    }.forEach {
                        it.percentage = response.percent
                    }
                    insertAchievementsList(allAchievements)
                }
            }, {
                Timber.e(it)
                insertAchievementsList(allAchievements)
            })

    }

    fun getAchievementsFromDb(): LiveData<List<Achievement>> = dao.getAchievements()

    fun getAchievementsFromApi(appId: String) = api.getSchemaForGame(appId)

    fun getAchievedStatusForAchievementsForGame(appId: String, allAchievements: List<Achievement>): LiveData<Resource<List<Achievement>>> {
        return object : NetworkBoundResource<List<Achievement>, AchievedAchievementResponse>(appExecutors) {
            // Save the new Achievements in the Database. Conflicts will be replaced.
            override fun saveCallResult(item: AchievedAchievementResponse) {
                val achievements = mutableListOf<Achievement>()
                item.playerStats.achievements
                    .asSequence()
                    .filter { it.achieved != 0 }
                    .map { ownedAchievement ->
                        allAchievements.filter {
                            it.name == ownedAchievement.apiName
                        }.forEach { it ->
                            it.unlockTime = ownedAchievement.getUnlockDate()
                            it.achieved = ownedAchievement.achieved != 0
                            it.description = ownedAchievement.description
                            achievements.add(it)
                        }
                    }
                    .toList()
                return dao.update(achievements)
            }

            override fun shouldFetch(data: List<Achievement>?) = data != null && data.isNotEmpty()
                && achievementsListRateLimit.shouldFetch("getAchievedStatusForAchievementsForGame_$appId")

            override fun loadFromDb(): LiveData<List<Achievement>> {
                return dao.getAchievements(appId)
            }

            override fun createCall(): LiveData<ApiResponse<AchievedAchievementResponse>> {
                // TODO Refactor invalid user id logic.
                return api.getAchievementsForPlayer(appId, userRepository.getCurrentPlayerId() ?: "-1")
            }

            override fun onFetchFailed() {
                achievementsListRateLimit.reset(appId)
            }

        }.asLiveData()
    }

    fun getGlobalAchievementStats(appId: String, achievements: List<Achievement>)
        : LiveData<Resource<List<Achievement>>> {
        return object : NetworkBoundResource<List<Achievement>, GlobalAchievResponse>(appExecutors) {
            override fun saveCallResult(item: GlobalAchievResponse) {
                item.achievementpercentages.achievements.forEach { response ->
                    achievements.filter {
                        it.name == response.name
                    }.forEach {
                        it.percentage = response.percent
                    }
                }
                return dao.update(achievements)
            }

            override fun shouldFetch(data: List<Achievement>?) = data != null && data.isNotEmpty() && achievementsListRateLimit.shouldFetch("GlobalAchievementStats_$appId")

            override fun loadFromDb(): LiveData<List<Achievement>> {
                return dao.getAchievements(appId)
            }

            override fun createCall(): LiveData<ApiResponse<GlobalAchievResponse>> {
                return api.getGlobalAchievementStats(appId)
            }
        }.asLiveData()
    }

    fun getAchievement(name: String, appId: String): LiveData<List<Achievement>> {
        return dao.getAchievement(name, appId)
    }

    //    fun getBestAchievementsDay(): Single<Pair<String, Int>> {
    //        return dao.getAchievements()
    //            .map { allAchievements ->
    //                // Create a list of Pairs<Date, Int>> from all emptyAchievements
    //                val newList = mutableListOf<Pair<String, Int>>()
    //                allAchievements
    //                    .filter {
    //                        it.achieved
    //                    }.forEach { achievement ->
    //                        val key = achievement.getDateStringNoTime()
    //                        newList.add(Pair(key, 1))
    //                    }
    //                newList
    //            }.map { pairs ->
    //                // Add and collect sizes for similar dates
    //                val achievementsByDate = HashMap<String, Int>()
    //                pairs.forEach { pair ->
    //                    var oldSize = achievementsByDate[pair.first]
    //                    oldSize = oldSize?.plus(pair.second) ?: pair.second
    //                    achievementsByDate[pair.first] = oldSize
    //                }
    //                achievementsByDate
    //            }.map {
    //                // Get the pair with the biggest size.
    //                var bestDay = Pair("No Achievements yet.", 0)
    //                it.forEach { t, u ->
    //                    if (u > bestDay.second) {
    //                        bestDay = Pair(t, u)
    //                    }
    //                }
    //
    //                bestDay
    //            }
    //    }

    private fun isNewDate(existingDate: Date, otherDate: Date): Boolean {
        val existingCalendat = Calendar.getInstance()
        existingCalendat.time = existingDate

        val otherCalendar = Calendar.getInstance()
        otherCalendar.time = otherDate
        return existingCalendat.get(Calendar.DAY_OF_MONTH) != otherCalendar.get(Calendar.DAY_OF_MONTH)
            || existingCalendat.get(Calendar.MONTH) != otherCalendar.get(Calendar.MONTH)
            || existingCalendat.get(Calendar.YEAR) != otherCalendar.get(Calendar.YEAR)
    }

    //    private fun getGlobalStats(getAppId: String, achievements: List<Achievement>): Single<List<Achievement>> {
    //        return api.getGlobalAchievementStats(getAppId).map { globalAchievResponse ->
    //            globalAchievResponse.achievementpercentages.achievements.map { response ->
    //                achievements.filter { it.getName == response.getName }.forEach { achievement ->
    //                    achievement.percentage = response.percent
    //                }
    //            }
    //
    //            achievements
    //        }
    //    }

    interface AchievementsListener {
        fun onAchievementsLoadedForGame(appId: String, achievements: List<Achievement>)
    }
}