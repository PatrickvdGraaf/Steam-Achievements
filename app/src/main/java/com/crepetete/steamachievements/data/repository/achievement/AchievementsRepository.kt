package com.crepetete.steamachievements.data.repository.achievement

import androidx.lifecycle.LiveData
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

    fun getAchievementsFromDb(): LiveData<List<Achievement>> = dao.getAchievementsAsLiveData()

    /**
     * Fetch all emptyAchievements for a specific game without their global completion rate and achieved
     * info.
     */
    fun loadAchievementsForGame(appId: String): LiveData<Resource<List<Achievement>>> {
        return object : NetworkBoundResource<List<Achievement>, SchemaResponse>(appExecutors) {
            /**
             * Save the new Achievements in the Database. Conflicts will be ignored.
             */
            override fun saveCallResult(item: SchemaResponse) {
                val achievements = item.game.availableGameStats?.achievements
                if (achievements != null) {

                    try {
                        dao.insert(achievements)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    onFetchFailed()
                }
            }

            override fun shouldFetch(data: List<Achievement>?) = data == null
                    || achievementsListRateLimit.shouldFetch("${appId}_achievements")

            override fun loadFromDb(): LiveData<List<Achievement>> {
                return dao.getAchievementsForGameAsLiveData(appId)
            }

            override fun createCall() = api.getSchemaForGameAsLiveData(appId)

            override fun onFetchFailed() {
                achievementsListRateLimit.reset(appId)
            }
        }.asLiveData()
    }

    /**
     *
     */
    fun getAchievedStatusForAchievementsForGame(appId: String,
                                                allAchievements: List<Achievement>): LiveData<Resource<List<Achievement>>> {
        return object : NetworkBoundResource<List<Achievement>, AchievedAchievementResponse>(appExecutors) {
            /**
             * Save the new Achievements in the Database. Conflicts will be replaced.
             */
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

            override fun loadFromDb(): LiveData<List<Achievement>>? {

                return dao.getAchievementsForGameAsLiveData(appId)
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