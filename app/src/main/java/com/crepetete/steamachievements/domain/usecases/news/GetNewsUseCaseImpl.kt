package com.crepetete.steamachievements.domain.usecases.news

import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.domain.repository.GameRepository

/**
 * Retrieves a list of NewsItems for the given game ID.
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 09 Jun, 2020; 23:23.
 */
class GetNewsUseCaseImpl(private val gameRepo: GameRepository) : GetNewsUseCase {
    override fun invoke(gameId: String): LiveResource<List<NewsItem>> {
        return gameRepo.getNews(gameId)
    }
}
