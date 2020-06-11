package com.crepetete.steamachievements.data.repository

import com.crepetete.data.network.SteamApiService
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.domain.repository.AchievementsRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

/**
 * @author: Patrick van de Graaf.
 * @date: Wed 10 Jun, 2020; 14:39.
 */
class AchievementsRepositoryImpl(
    private val api: SteamApiService,
    private val achievementsDao: AchievementsDao
) : AchievementsRepository {
    override suspend fun updateAchievementsFromApi(userId: String, appId: String) {
        try {
            val baseResponse = api.getSchemaForGame(appId)

            /* Reference to base achievements list. */
            val responseAchievements = baseResponse.game.availableGameStats?.achievements

            /* Iterate over all object in the response.  */
            if (responseAchievements?.isNotEmpty() == true) {
                try {
                    val achievedResponse = api.getAchievementsForPlayer(appId, userId)

                    achievedResponse.playerStats?.achievements?.forEach { response ->
                        /* For each one, find the corresponding achievement in the
                           responseAchievements list and update the information. */
                        responseAchievements.filter { achievement ->
                            achievement.name == response.apiName
                        }.forEach { resultAchievement ->
                            resultAchievement.appId = appId.toLong()
                            resultAchievement.unlockTime = response.getUnlockDate()
                            resultAchievement.achieved = response.achieved != 0
                            response.description?.let { desc ->
                                resultAchievement.description = desc
                            }
                        }
                    }

                    val globalResponse = api.getGlobalAchievementStats(appId)

                    globalResponse.achievementpercentages.achievements.forEach { response ->
                        responseAchievements.filter { achievement ->
                            achievement.name == response.name
                        }
                            .forEach { game ->
                                game.percentage = response.percent
                            }
                    }
                } catch (e: Exception) {
                    Timber.d(e)
                }
            }
            responseAchievements?.let { achievementsDao.upsert(it) }
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    override suspend fun updateAchievementsFromApi(userId: String, appIds: List<String>) {
        appIds.forEach { appId ->
            val baseResponse = api.getSchemaForGame(appId)

            /* Reference to base achievements list. */
            val responseAchievements = baseResponse.game.availableGameStats?.achievements

            /* Iterate over all object in the response.  */
            if (responseAchievements?.isNotEmpty() == true) {
                val achievedResponse = api.getAchievementsForPlayer(appId, userId)

                achievedResponse.playerStats?.achievements?.forEach { response ->
                    /* For each one, find the corresponding achievement in the
                       responseAchievements list and update the information. */
                    responseAchievements.filter { achievement ->
                        achievement.name == response.apiName
                    }.forEach { resultAchievement ->
                        resultAchievement.appId = appId.toLong()
                        resultAchievement.unlockTime = response.getUnlockDate()
                        resultAchievement.achieved = response.achieved != 0
                        response.description?.let { desc ->
                            resultAchievement.description = desc
                        }
                    }
                }

                val globalResponse = api.getGlobalAchievementStats(appId)

                globalResponse.achievementpercentages.achievements.forEach { response ->
                    responseAchievements.filter { achievement ->
                        achievement.name == response.name
                    }
                        .forEach { game ->
                            game.percentage = response.percent
                        }
                }
            }
            achievementsDao.upsert(responseAchievements ?: listOf())
        }
    }

    override fun getAchievementsAsFlow(): Flow<List<Achievement>> {
        return achievementsDao.getAchievementsFlow()
    }

    override fun getAchievementsForGameAsFlow(appId: String): Flow<List<Achievement>> {
        return achievementsDao.getAchievementsFlow(appId)
    }
}
