package com.crepetete.steamachievements.data.repository

import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.helper.NetworkBoundResource
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.domain.repository.AchievementsRepository
import com.crepetete.steamachievements.domain.usecases.player.GetCurrentPlayerIdUseCase
import io.reactivex.Observable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.Calendar
import java.util.Date

/**
 * @author: Patrick van de Graaf.
 * @date: Wed 10 Jun, 2020; 14:39.
 */
class AchievementsRepositoryImpl(
    private val api: SteamApiService,
    private val achievementsDao: AchievementsDao,
    private val gamesDao: GamesDao,
    private val getCurrentPlayerIdUseCase: GetCurrentPlayerIdUseCase
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
                            resultAchievement.appId = appId.toInt()
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
            responseAchievements?.let {
                Timber.d("LIVEDATA TEST: Updating Achievements in Database")
                achievementsDao.upsert(it)
            }
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    override suspend fun updateAchievementsFromApi(userId: String, appIds: List<String>) {
        val updatedAchievements = mutableListOf<Achievement>()
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
                        resultAchievement.appId = appId.toInt()
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
            if (achievementsDao.getAchievements(appId).isEmpty()) {
                // If no achievements were available, update the database immediately
                achievementsDao.upsert(responseAchievements ?: listOf())
            } else {
                // Else, update them at the end of the update call.
                updatedAchievements.addAll(responseAchievements ?: listOf())
            }
        }

        if (updatedAchievements.isNotEmpty()) {
            Timber.d("LIVEDATA TEST: Updating Achievements in Database")
            achievementsDao.upsert(updatedAchievements)
        }
    }


    private var lastFetchDate: Date? = null

    private companion object {
        const val MAX_REFRESH_MILLIS = 1000 * 60 * 15
    }

    @FlowPreview
    fun getAchievements(): Flow<Resource<List<Achievement>>> {
        return object : NetworkBoundResource<List<Achievement>>() {
            override suspend fun loadFromDb(): List<Achievement> {
                return achievementsDao.getAchievements()
            }

            override suspend fun getDataFetchDate(data: List<Achievement>?): Date? {
                return lastFetchDate
            }

            override suspend fun shouldFetch(
                data: List<Achievement>?,
                dataFetchDate: Date?
            ): Boolean {
                val millisSinceLastUpdate =
                    Calendar.getInstance().timeInMillis - (lastFetchDate?.time ?: 0L)
                return millisSinceLastUpdate < MAX_REFRESH_MILLIS
            }

            override suspend fun fetchFromNetwork(): List<Achievement>? {
                return null
            }

            override suspend fun saveCallResult(data: List<Achievement>?) {
                TODO("Not yet implemented")
            }

            override fun zipRequests(): List<Observable<*>> {
                return super.zipRequests()
            }

        }.asFlow()
    }

    @FlowPreview
    suspend fun fetchItems(itemIds: Iterable<String>, userId: String): Flow<List<Achievement>> =
        itemIds
            .asFlow()
            .flatMapMerge { itemId ->
                flow { emit(getAchievementsFromApi(itemId, userId)) }
            }

    private suspend fun getAchievementsFromApi(): List<Achievement> {
        val achievements = mutableListOf<Achievement>()
        getCurrentPlayerIdUseCase()?.let { userId ->
            gamesDao.getGameIds(userId).forEach { gameId ->
                achievements.addAll(getAchievements(gameId, userId))
            }
        }

        return achievements
    }

    private suspend fun getAchievements(appId: String, userId: String): List<Achievement>? {
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
                        resultAchievement.appId = appId.toInt()
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

        return responseAchievements
    }

    override fun getAchievementsAsFlow(): Flow<List<Achievement>> {
        return achievementsDao.getAchievementsFlow()
    }

    override fun getAchievementsForGameAsFlow(appId: String): Flow<List<Achievement>> {
        return achievementsDao.getAchievementsFlow(appId)
    }
}
