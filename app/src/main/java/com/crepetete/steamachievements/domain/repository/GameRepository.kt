package com.crepetete.steamachievements.domain.repository

import androidx.lifecycle.LiveData
import com.crepetete.data.helper.LiveResource
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Game

interface GameRepository {
    fun getGames(userId: String): LiveResource<List<Game>>
    fun getGame(appId: String): LiveData<Game>
    fun update(item: BaseGameInfo)
    suspend fun fetchAchievementsFromApi(userId: String, appId: String): List<Achievement>?

    // TODO move to some kind of news viewmodel?
    suspend fun getNews(appId: String): LiveResource<List<NewsItem>>
}