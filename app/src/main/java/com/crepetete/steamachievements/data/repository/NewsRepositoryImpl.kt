package com.crepetete.steamachievements.data.repository

import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.api.helper.NetworkBoundResource
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.data.api.response.news.NewsResponse
import com.crepetete.steamachievements.data.database.dao.NewsDao
import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.Call

/**
 * @author: Patrick van de Graaf.
 * @date: Fri 12 Jun, 2020; 13:20.
 */
class NewsRepositoryImpl(
    private val api: SteamApiService,
    private val newsDao: NewsDao
) : NewsRepository {
    /**
     * Returns current Database NewsItems for the corresponding [appId].
     * As per best practice, the result we request from Room is returned as a Flow.
     */
    override fun getNewsAsFlow(appId: String): Flow<List<NewsItem>> {
        return newsDao.getNewsForGameAsFlow(appId)
    }

    override fun updateNews(appId: String): LiveResource {
        return object : NetworkBoundResource<List<NewsItem>, NewsResponse>() {
            override suspend fun createCall(): Call<NewsResponse> {
                return api.getNews(appId)
            }

            override suspend fun saveCallResult(data: NewsResponse?) {
                data?.appNews?.newsItems?.let { news -> newsDao.upsert(news) }
            }
        }.asLiveResource()
    }
}