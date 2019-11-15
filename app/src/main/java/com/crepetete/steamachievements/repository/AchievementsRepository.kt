package com.crepetete.steamachievements.repository

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.api.SteamApiService
import com.crepetete.steamachievements.db.dao.AchievementsDao
import com.crepetete.steamachievements.repository.limiter.RateLimiter
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

    suspend fun fetchAchievementsFromApi(appId: String) {
        if (!achievementsListRateLimit.shouldFetch(appId)) {
            return
        }

        try {
            val baseResponse = api.getSchemaForGame(appId)

            /* Reference to base achievements list. */
            val responseAchievements = baseResponse.game.availableGameStats?.achievements ?: mutableListOf()

            /* Iterate over all object in the response.  */
            try {
                val achievedResponse = api.getAchievementsForPlayer(appId, userRepository.getCurrentPlayerId())
                achievedResponse.playerStats?.achievements?.forEach { response ->

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
            } catch (e: Exception) {
                Timber.d(e)
            }

            try {
                val globalResponse = api.getGlobalAchievementStats(appId)
                globalResponse.achievementpercentages.achievements.forEach { response ->
                    responseAchievements.filter { game -> game.name == response.name }
                        .forEach { game ->
                            game.percentage = response.percent
                        }
                }
            } catch (e: Exception) {
                Timber.d(e)
            }

            responseAchievements.forEach { achievement ->
                try {
                    achievement.appId = appId.toLong()
                } catch (e: NumberFormatException) {
                    Timber.e(e)
                }
                Timber.d(achievement.toString())
            }
            dao.insert(responseAchievements)
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    fun getAchievements(appId: String): List<Achievement> {
        return dao.getAchievements(appId)
    }

    fun fetchAllAchievements(): LiveData<List<Achievement>?> {
        return dao.getAchievements()
    }
}
