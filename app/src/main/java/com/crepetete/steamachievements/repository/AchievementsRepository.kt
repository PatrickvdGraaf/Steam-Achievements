package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.ApiErrorResponse
import com.crepetete.steamachievements.api.response.ApiResponse
import com.crepetete.steamachievements.api.response.ApiSuccessResponse
import com.crepetete.steamachievements.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.db.dao.AchievementsDao
import com.crepetete.steamachievements.repository.limiter.RateLimiter
import com.crepetete.steamachievements.repository.resource.NetworkBoundResource
import com.crepetete.steamachievements.testing.OpenForTesting
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

    fun getAchievements(appId: String, listener: PrivateProfileMessageListener): LiveData<Resource<List<Achievement>>> {
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

            /**
             * Zips the results of three separate api calls for Achievement data and merges them.
             *
             * [SteamApiService.getSchemaForGame] for the general / base data for the achievements.
             * [SteamApiService.getAchievementsForPlayer] for the players achieved status for each achievement.
             * [SteamApiService.getGlobalAchievementStats] for global stats for each achievement.
             */
            override fun createCall(): LiveData<ApiResponse<SchemaResponse>> = zip3(
                api.getSchemaForGame(appId),
                api.getAchievementsForPlayer(
                    appId,
                    userRepository.getCurrentPlayerId()),
                api.getGlobalAchievementStats(appId)) { baseResponse, achievedResponse, globalResponse ->

                /* Check if the base achievement call response was successful. */
                if (baseResponse is ApiSuccessResponse) {
                    /* Reference to base achievements list. */
                    val responseAchievements = baseResponse.body.game.availableGameStats?.achievements ?: mutableListOf()

                    /* Zip Achieved Stats into the base achievements list if the request was successful. */
                    if (achievedResponse is ApiSuccessResponse) {

                        /* Iterate over all object in the response.  */
                        achievedResponse.body.playerStats.achievements.forEach { response ->

                            /* For each one, find the corresponding achievement in the responseAchievements list
                             and update the information. */
                            responseAchievements.filter { achievement -> achievement.name == response.apiName }
                                .forEach { resultAchievement ->
                                    resultAchievement.unlockTime = response.getUnlockDate()
                                    resultAchievement.achieved = response.achieved != 0
                                    response.description?.let { desc ->
                                        resultAchievement.description = desc
                                    }
                                }
                        }
                    } else if (achievedResponse is ApiErrorResponse) {
                        Timber.e(achievedResponse.errorMessage)
                        if (achievedResponse.errorMessage?.contains("Profile is not public") == true) {
                            listener.onPrivateModelMessage()
                        }
                    }

                    /* Check if the global achievement stats call response was successful. */
                    if (globalResponse is ApiSuccessResponse) {
                        globalResponse.body.achievementpercentages.achievements.forEach { response ->
                            responseAchievements.filter { game -> game.name == response.name }
                                .forEach { game -> game.percentage = response.percent }
                        }
                    }
                }

                baseResponse
            }

            override fun onFetchFailed() {
                achievementsListRateLimit.reset(appId)
            }
        }.asLiveData()
    }

    /**
     * zip3 function takes three LiveData objects, src1, src2 and src3. These are used as input source for MediatorLiveData to create.
     * And also it takes zipper function as its last parameter, and it should describe how the data would be zipped into
     * one object R. In the updateValueIfNeeded() function, it checks version and null, and then set MediatorLiveData’s value.
     *
     * Created at 01 February, 2019.
     */
    private fun <T1, T2, T3, R> zip3(
        src1: LiveData<T1>,
        src2: LiveData<T2>,
        src3: LiveData<T3>,
        zipper: (T1, T2, T3) -> R): LiveData<R> = MediatorLiveData<R>().apply {

        var src1Version = 0
        var src2Version = 0
        var src3Version = 0

        var lastSrc1: T1? = null
        var lastSrc2: T2? = null
        var lastSrc3: T3? = null

        fun updateValueIfNeeded() {
            val immutableSrc1 = lastSrc1
            val immutableSrc2 = lastSrc2
            val immutableSrc3 = lastSrc3
            if (src1Version > 0 && src2Version > 0 && src3Version > 0 &&
                immutableSrc1 != null && immutableSrc2 != null && immutableSrc3 != null) {
                value = zipper(immutableSrc1, immutableSrc2, immutableSrc3)
                src1Version = 0
                src2Version = 0
                src3Version = 0
            }
        }

        addSource(src1) {
            lastSrc1 = it
            src1Version++
            updateValueIfNeeded()
        }

        addSource(src2) {
            lastSrc2 = it
            src2Version++
            updateValueIfNeeded()
        }

        addSource(src3) {
            lastSrc3 = it
            src3Version++
            updateValueIfNeeded()
        }
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

    interface PrivateProfileMessageListener {
        fun onPrivateModelMessage()
    }
}