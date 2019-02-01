package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.ApiResponse
import com.crepetete.steamachievements.api.response.ApiSuccessResponse
import com.crepetete.steamachievements.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.db.dao.AchievementsDao
import com.crepetete.steamachievements.repository.limiter.RateLimiter
import com.crepetete.steamachievements.repository.resource.NetworkBoundResource
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.util.livedata.zip3
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Resource
import timber.log.Timber
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

    // TODO Merge all individual getAchievements method so it can accept a list of appIds.
    fun getAchievements(appId: String): LiveData<Resource<List<Achievement>>> {
        return object : NetworkBoundResource<List<Achievement>, SchemaResponse>(appExecutors) {

            override fun saveCallResult(item: SchemaResponse) {
                Timber.d("Saving Achievements in DB for getAppId: $appId")

                val achievements = item.game.availableGameStats?.achievements
                if (achievements != null) {
                    achievements.forEach {
                        it.appId = appId
                    }
                    dao.upsert(achievements)
                }
            }

            override fun shouldFetch(data: List<Achievement>?) = achievementsListRateLimit.shouldFetch("ACHIEVEMENTS_$appId")

            override fun loadFromDb(): LiveData<List<Achievement>> = dao.getAchievements(appId)

            override fun createCall(): LiveData<ApiResponse<SchemaResponse>> = zip3(
                api.getSchemaForGame(appId),
                api.getAchievementsForPlayer(appId, userRepository.getCurrentPlayerId() ?: "-1"),
                api.getGlobalAchievementStats(appId)
            ) { baseResponse, achievedResponse, globalResponse ->

                /* Check if the base achievement call response was successful. */
                if (baseResponse is ApiSuccessResponse) {
                    /* Reference to base achievements list. */
                    val responseAchievements = baseResponse.body.game.availableGameStats?.achievements ?: mutableListOf()

                    /* Zip Achieved Stats into the base achievements list if the request was successful. */
                    if (achievedResponse is ApiSuccessResponse) {
                        achievedResponse.body.playerStats.achievements.forEach { achievedAchievement ->
                            /* Iterate over all object in the response. For each one, find the corresponding achievement in
                             * the responseAchievements list and update the information. */
                            responseAchievements.filter { achievement -> achievement.name == achievedAchievement.apiName }
                                .forEach { resultAchievement ->
                                    resultAchievement.unlockTime = achievedAchievement.getUnlockDate()
                                    resultAchievement.achieved = achievedAchievement.achieved != 0
                                    if (achievedAchievement.description != null) {
                                        resultAchievement.description = achievedAchievement.description
                                    }
                                }
                        }
                    }

                    /* Check if the global achievement stats call response was successful. */
                    if (globalResponse is ApiSuccessResponse) {
                        globalResponse.body.achievementpercentages.achievements.forEach { r ->
                            responseAchievements.filter { it.name == r.name }
                                .forEach { it.percentage = r.percent }
                        }
                    }

                    if (appId == "292030") {
                        Timber.d("breakpoint")
                    }
                }
                return@zip3 baseResponse
            }

            override fun onFetchFailed() {
                achievementsListRateLimit.reset(appId)
            }
        }.asLiveData()
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

    //    private fun isNewDate(existingDate: Date, otherDate: Date): Boolean {
    //        val existingCalendat = Calendar.getInstance()
    //        existingCalendat.time = existingDate
    //
    //        val otherCalendar = Calendar.getInstance()
    //        otherCalendar.time = otherDate
    //        return existingCalendat.get(Calendar.DAY_OF_MONTH) != otherCalendar.get(Calendar.DAY_OF_MONTH)
    //            || existingCalendat.get(Calendar.MONTH) != otherCalendar.get(Calendar.MONTH)
    //            || existingCalendat.get(Calendar.YEAR) != otherCalendar.get(Calendar.YEAR)
    //    }
}