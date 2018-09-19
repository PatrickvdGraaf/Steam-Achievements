package com.crepetete.steamachievements.data.repository.achievement

import android.arch.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.response.ApiResponse
import com.crepetete.steamachievements.data.api.response.achievement.AchievedAchievementResponse
import com.crepetete.steamachievements.data.api.response.achievement.GlobalAchievResponse
import com.crepetete.steamachievements.data.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.repository.user.UserRepository
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.utils.RateLimiter
import com.crepetete.steamachievements.utils.resource.NetworkBoundResource
import com.crepetete.steamachievements.utils.resource.Resource
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles [Achievement] objects.
 */
@Singleton
class AchievementsRepository @Inject constructor(
        private val appExecutors: AppExecutors,
        private val userRepository: UserRepository,
        private val dao: AchievementsDao,
        private val api: SteamApiService) {

    private val achievementsListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun loadAchievementsForGame(appId: String): LiveData<Resource<List<Achievement>>> {
        return object : NetworkBoundResource<List<Achievement>, SchemaResponse>(appExecutors) {
            override fun saveCallResult(item: SchemaResponse) {
                val achievements = item.game.availableGameStats?.achievements
                if (achievements != null) {
                    achievements.forEach {
                        it.appId = appId
                    }

//                    appExecutors.mainThread().execute {
//                        // Get the Achieved status when new achievements are loaded from the API.
//                        getAchievedStatusForAchievementsForGame(appId, achievements)
//                    }

                    try {
                        dao.insert(achievements)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun shouldFetch(data: List<Achievement>?) = data == null
                    || data.isEmpty()
                    || achievementsListRateLimit.shouldFetch(appId)


            override fun loadFromDb() = dao.getAchievementsForGameAsLiveData(appId)

            override fun createCall() = api.getSchemaForGameAsLiveData(appId)

            override fun onFetchFailed() {
                achievementsListRateLimit.reset(appId)
            }
        }.asLiveData()
    }

    fun getAchievedStatusForAchievementsForGame(appId: String,
                                                allAchievements: List<Achievement>): LiveData<Resource<List<Achievement>>> {
        return object : NetworkBoundResource<List<Achievement>, AchievedAchievementResponse>(appExecutors) {
            override fun saveCallResult(item: AchievedAchievementResponse) {
                val achievements = mutableListOf<Achievement>()
                item.playerStats.achievements
                        .filter { it.achieved != 0 }
                        .map { ownedAchievement ->
                            allAchievements.filter {
                                it.name == ownedAchievement.apiName
                            }.forEach {
                                it.unlockTime = ownedAchievement.getUnlockDate()
                                it.achieved = ownedAchievement.achieved != 0
                                achievements.add(it)
                            }
                        }
                return dao.update(achievements)
            }

            override fun shouldFetch(data: List<Achievement>?) = true

            override fun loadFromDb(): LiveData<List<Achievement>> {
                return dao.getAchievementsAsLiveData()
            }

            override fun createCall(): LiveData<ApiResponse<AchievedAchievementResponse>> {
                return api.getAchievementsForPlayerAsLiveData(appId, userRepository.getUserId())
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
                item.achievementpercentages.achievements.forEach {response ->
                    achievements.filter {
                        it.name == response.name
                    }.forEach {
                        it.percentage = response.percent
                    }
                }
                return dao.update(achievements)
            }

            override fun shouldFetch(data: List<Achievement>?): Boolean {
                return true
            }

            override fun loadFromDb(): LiveData<List<Achievement>> {
                return dao.getAchievementsForGameAsLiveData(appId)
            }

            override fun createCall(): LiveData<ApiResponse<GlobalAchievResponse>> {
                return api.getGlobalAchievementStatsAsLiveData(appId)
            }
        }.asLiveData()
    }

    fun getAchievement(name: String, appId: String): LiveData<List<Achievement>> {
        return dao.getAchievement(name, appId)
    }
}