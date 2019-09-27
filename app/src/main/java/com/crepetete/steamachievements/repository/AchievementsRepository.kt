package com.crepetete.steamachievements.repository

import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.api.response.ApiErrorResponse
import com.crepetete.steamachievements.api.response.ApiSuccessResponse
import com.crepetete.steamachievements.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.db.dao.AchievementsDao
import com.crepetete.steamachievements.repository.limiter.RateLimiter
import com.crepetete.steamachievements.repository.resource.LiveResource
import com.crepetete.steamachievements.repository.resource.NetworkBoundResource
import com.crepetete.steamachievements.testing.OpenForTesting
import com.crepetete.steamachievements.vo.Achievement
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OpenForTesting
class AchievementsRepository @Inject constructor(
    private val userRepository: UserRepository,
    private val dao: AchievementsDao,
    private val api: SteamApiService
) : BaseRepository() {

    private val achievementsListRateLimit = RateLimiter<String>(15, TimeUnit.MINUTES)

    suspend fun getAchievements(appId: Long, listener: AchievementsErrorListener): LiveResource<List<Achievement>> {
        return object : NetworkBoundResource<List<Achievement>, SchemaResponse>() {

            override suspend fun saveCallResult(data: SchemaResponse) {
                Timber.d("Saving Achievements in DB for getAppId: $appId")

                val achievements = data.game.availableGameStats?.achievements
                if (achievements != null) {
                    achievements.forEach {
                        it.appId = appId
                    }
                    dao.upsert(achievements)
                }
            }

            override fun shouldFetch(data: List<Achievement>?) = achievementsListRateLimit.shouldFetch("ACHIEVEMENTS_$appId")

            /**
             * Zips the results of three separate api calls for Achievement data and merges them.
             *
             * [SteamApiService.getSchemaForGame] for the general / base data for the achievements.
             * [SteamApiService.getAchievementsForPlayer] for the players achieved status for each achievement.
             * [SteamApiService.getGlobalAchievementStats] for global stats for each achievement.
             */
            override suspend fun createCall(): SchemaResponse? {
                val baseResponse = api.getSchemaForGame(appId.toString())
                val achievedResponse = api.getAchievementsForPlayer(appId.toString(),
                    userRepository.getCurrentPlayerId())
                val globalResponse = api.getGlobalAchievementStats(appId.toString())

                /* Check if the base achievement call response was successful. */
                if (baseResponse is ApiSuccessResponse) {
                    /* Reference to base achievements list. */
                    val responseAchievements = baseResponse.body.game.availableGameStats?.achievements ?: mutableListOf()

                    /* Zip Achieved Stats into the base achievements list if the request was successful. */
                    if (achievedResponse is ApiSuccessResponse) {

                        /* Iterate over all object in the response.  */
                        achievedResponse.body.playerStats?.achievements?.forEach { response ->

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
                            listener.onPrivateProfileErrorMessage()
                        }
                    }

                    /* Check if the global achievement stats call response was successful. */
                    if (globalResponse is ApiSuccessResponse) {
                        globalResponse.body.achievementpercentages.achievements.forEach { response ->
                            responseAchievements.filter { game -> game.name == response.name }
                                .forEach { game -> game.percentage = response.percent }
                        }
                    }

                    return baseResponse.body
                }
                return null
            }

            override suspend fun loadFromDb(): List<Achievement> = dao.getAchievements(appId.toString())

        }.asLiveResource()
    }

    interface AchievementsErrorListener {
        fun onPrivateProfileErrorMessage()
    }
}