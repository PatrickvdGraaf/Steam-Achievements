package com.crepetete.steamachievements.api.response.news

import com.squareup.moshi.Json

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 08 Dec, 2019; 01:01.
 */
data class NewsResponse(
    private val appNews: AppNews
) {
    fun getNews(): List<NewsItem> {
        return appNews.newsItems ?: listOf()
    }
}

/**
 * @param appId, the AppID of the game you want news of news items, an array of news item
 * information:
 * - An ID, title and url.
 * - A shortened excerpt of the contents (to maxlength characters), terminated by "..." if longer
 *   than maxLength.
 * - A comma-separated string of labels and UNIX timestamp.
 */
data class AppNews(
    @Json(name = "game_count")
    val appId: String,
    @Json(name = "newsitems")
    val newsItems: List<NewsItem>?
)

/**
 * @param author can be empty.
 * @param contents is returned as attributed text.
 */
data class NewsItem(
    val gid: String,
    val title: String,
    val url: String,
    @Json(name = "is_external_url")
    val isExternalUrl: Boolean,
    val author: String,
    val contents: String,
    @Json(name = "feedlabel")
    val feedLabel: String,
    val date: Int,
    @Json(name = "feedname")
    val feedName: String,
    @Json(name = "feed_type")
    val feedType: String,
    @Json(name = "appid")
    val appId: String
)