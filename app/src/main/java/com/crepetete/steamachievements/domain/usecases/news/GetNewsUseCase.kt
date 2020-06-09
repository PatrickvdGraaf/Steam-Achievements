package com.crepetete.steamachievements.domain.usecases.news

import com.crepetete.data.helper.LiveResource
import com.crepetete.steamachievements.data.api.response.news.NewsItem

/**
 * Retrieves a list of NewsItems for the given game ID.
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 09 Jun, 2020; 23:22.
 */
interface GetNewsUseCase {
    operator fun invoke(gameId: String): LiveResource<List<NewsItem>>
}