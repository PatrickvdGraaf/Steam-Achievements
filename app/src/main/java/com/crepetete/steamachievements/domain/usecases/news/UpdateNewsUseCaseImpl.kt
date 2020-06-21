package com.crepetete.steamachievements.domain.usecases.news

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.repository.NewsRepository

/**
 * Retrieves a list of NewsItems for the given game ID.
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 09 Jun, 2020; 23:23.
 */
class UpdateNewsUseCaseImpl(private val newsRepository: NewsRepository) : UpdateNewsUseCase {
    override fun invoke(gameId: String): LiveData<Resource> {
        return newsRepository.updateNews(gameId)
    }
}
