package com.crepetete.steamachievements.domain.repository

import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.data.helper.LiveResource
import kotlinx.coroutines.flow.Flow

/**
 * @author: Patrick van de Graaf.
 * @date: Fri 12 Jun, 2020; 13:12.
 */
interface NewsRepository {
    fun getNewsAsFlow(appId: String): Flow<List<NewsItem>>
    fun updateNews(appId: String): LiveResource
}