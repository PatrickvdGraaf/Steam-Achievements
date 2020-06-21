package com.crepetete.steamachievements.domain.usecases.news

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.data.api.response.news.NewsItem

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Fri 12 Jun, 2020; 13:11.
 */
interface GetNewsSnapshotUseCase {
    operator fun invoke(appId: String): LiveData<List<NewsItem>>
}