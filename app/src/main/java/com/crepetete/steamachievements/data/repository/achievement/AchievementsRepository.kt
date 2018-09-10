package com.crepetete.steamachievements.data.repository.achievement

import android.arch.lifecycle.LiveData
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
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
        private val dao: AchievementsDao,
        private val api: SteamApiService) {

    private val achievementsListRateLimit = RateLimiter<String>(10, TimeUnit.MINUTES)

    fun loadAchievementsForGame(appId: String): LiveData<Resource<List<Achievement>>> {
        return object : NetworkBoundResource<List<Achievement>, SchemaResponse>(appExecutors) {
            override fun saveCallResult(item: SchemaResponse) {
                val achievements = item.game.availableGameStats?.achievements
                if (achievements != null) {
                    try {
                        dao.insert(achievements)
                    }catch (e: Exception){
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
}