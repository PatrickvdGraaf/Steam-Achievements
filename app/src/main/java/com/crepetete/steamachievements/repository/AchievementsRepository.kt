package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.ApiResponse
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.achievement.AchievedAchievementResponse
import com.crepetete.steamachievements.api.response.achievement.GlobalAchievResponse
import com.crepetete.steamachievements.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.db.dao.AchievementsDao
import com.crepetete.steamachievements.repository.user.UserRepository
import com.crepetete.steamachievements.util.RateLimiter
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Resource
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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
            // Save the new Achievements in the Database. Conflicts will be ignored.
            override fun saveCallResult(item: SchemaResponse) {
                Timber.d("Saving Achievements in DB for appId: $appId")

                val achievements = item.game.availableGameStats?.achievements
                if (achievements != null) {
                    dao.insert(achievements)
                }
            }

            override fun shouldFetch(data: List<Achievement>?) = data == null
                || data.isEmpty()

            override fun loadFromDb(): LiveData<List<Achievement>> {
                Timber.d("Getting achievements from DB for appId: $appId")
                return dao.getAchievements(appId)
            }

            override fun createCall(): LiveData<ApiResponse<SchemaResponse>> = api.getSchemaForGameAsLiveData(appId)

        }.asLiveData()
    }

    fun getAchievementsFromDb(): LiveData<List<Achievement>> = dao.getAchievements()

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
                            it.name == ownedAchievement.name
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

            override fun shouldFetch(data: List<Achievement>?): Boolean {
                return data == null
                    || achievementsListRateLimit.shouldFetch("${appId}_achieved_stats")
            }

            override fun loadFromDb(): LiveData<List<Achievement>> {

                return dao.getAchievements(appId)
            }

            override fun createCall(): LiveData<ApiResponse<AchievedAchievementResponse>> {
                // TODO Refactor invalid user id logic.
                return api.getAchievementsForPlayerAsLiveData(appId, userRepository.getCurrentPlayerId() ?: "-1")
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

            override fun shouldFetch(data: List<Achievement>?): Boolean {
                return achievementsListRateLimit.shouldFetch("${appId}_global_stats")
            }

            override fun loadFromDb(): LiveData<List<Achievement>> {
                return dao.getAchievements(appId)
            }

            override fun createCall(): LiveData<ApiResponse<GlobalAchievResponse>> {
                return api.getGlobalAchievementStatsAsLiveData(appId)
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

    //    private fun getGlobalStats(appId: String, achievements: List<Achievement>): Single<List<Achievement>> {
    //        return api.getGlobalAchievementStats(appId).map { globalAchievResponse ->
    //            globalAchievResponse.achievementpercentages.achievements.map { response ->
    //                achievements.filter { it.name == response.name }.forEach { achievement ->
    //                    achievement.percentage = response.percent
    //                }
    //            }
    //
    //            achievements
    //        }
    //    }
}