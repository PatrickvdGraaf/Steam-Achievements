package com.crepetete.steamachievements.data.repository.achievement

import android.arch.lifecycle.LiveData
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.response.ApiResponse
import com.crepetete.steamachievements.data.api.response.ApiSuccessResponse
import com.crepetete.steamachievements.data.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.utils.AppExecutors
import com.crepetete.steamachievements.utils.RateLimiter
import com.crepetete.steamachievements.utils.resource.NetworkBoundResource
import com.crepetete.steamachievements.utils.resource.Resource
import timber.log.Timber
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
                    dao.insert(achievements)
                }
            }

            override fun shouldFetch(data: List<Achievement>?): Boolean {
                Timber.i(data?.toString() ?: "No data")
                return data == null || data.isEmpty() || achievementsListRateLimit.shouldFetch(appId)
            }

            override fun loadFromDb(): LiveData<List<Achievement>> = dao.getAchievementsForGameAsLiveData(appId)

            override fun createCall(): LiveData<ApiResponse<SchemaResponse>> = api.getSchemaForGameAsLiveData(appId)

            override fun onFetchFailed() {
                achievementsListRateLimit.reset(appId)
            }

            override fun processResponse(response: ApiSuccessResponse<SchemaResponse>): SchemaResponse {
                if (BuildConfig.DEBUG){
                    Timber.i(response.body.toString())
                }
                return super.processResponse(response)
            }
        }.asLiveData()
    }
}