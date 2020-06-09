package com.crepetete.steamachievements.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.testing.OpenForTesting

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Wed 11 Dec, 2019; 16:16.
 */
@Dao
@OpenForTesting
abstract class NewsDao : BaseDao<NewsItem>() {
    @Query("SELECT * FROM news WHERE appId = :appId")
    abstract suspend fun getNewsForGame(appId: String): List<NewsItem>?
}