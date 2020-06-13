package com.crepetete.steamachievements.domain.usecases.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.domain.repository.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Fri 12 Jun, 2020; 13:12.
 */
class GetNewsSnapshotUseCaseImpl(
    private val newsRepository: NewsRepository
) : GetNewsSnapshotUseCase {
    @ExperimentalCoroutinesApi
    override fun invoke(appId: String): LiveData<List<NewsItem>> {
        return newsRepository.getNewsAsFlow(appId)
            .take(3)
            .flowOn(Dispatchers.Default)
            .asLiveData()
    }
}