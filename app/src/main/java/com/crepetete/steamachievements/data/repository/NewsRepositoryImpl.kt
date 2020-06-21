package com.crepetete.steamachievements.data.repository

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.helper.NetworkBoundResource
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.data.api.response.news.NewsResponse
import com.crepetete.steamachievements.data.database.dao.NewsDao
import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.Call
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * @author: Patrick van de Graaf.
 * @date: Fri 12 Jun, 2020; 13:20.
 */
class NewsRepositoryImpl(
    private val api: SteamApiService,
    private val newsDao: NewsDao
) : NewsRepository {

    // TODO move to Shared Preferences? Persist.
    private var gameLastFetchedDate: Date? = null

    /**
     * Returns current Database NewsItems for the corresponding [appId].
     * As per best practice, the result we request from Room is returned as a Flow.
     */
    override fun getNewsAsFlow(appId: String): Flow<List<NewsItem>> {
        return newsDao.getNewsForGameAsFlow(appId)
    }

    override fun updateNews(appId: String): LiveData<Resource<List<NewsItem>>> {
        return object : NetworkBoundResource<List<NewsItem>, NewsResponse>() {
            override suspend fun fetchFromNetwork(): Call<NewsResponse> = api.getNews(appId)

            override suspend fun saveCallResult(data: NewsResponse?) {
                // We don't save news to our database.
            }

            // We don't save news to our database.
            override suspend fun loadFromDb(): List<NewsItem>? = null

            override suspend fun getDataFetchDate(data: List<NewsItem>?) = gameLastFetchedDate

            override suspend fun shouldFetch(data: List<NewsItem>?, dataFetchDate: Date?): Boolean {
                return if (data != null) {
                    dataFetchDate.let {
                        if (it != null) {
                            Date().time - it.time > TimeUnit.MINUTES.toMillis(15)
                        } else {
                            //Always reload if this is the first time to load
                            true
                        }
                    }
                } else {
                    true
                }
            }
        }.result
    }
}