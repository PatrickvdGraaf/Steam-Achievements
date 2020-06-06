package com.crepetete.steamachievements.api.response.news

import androidx.room.Entity
import com.squareup.moshi.Json

/**
 * @author: Patrick van de Graaf.
 * @date: Sun 08 Dec, 2019; 01:01.
 */
data class NewsResponse(
    @field:Json(name = "appnews")
    val appNews: AppNews
)

/**
 * @param appId The AppID of the game you want news of news items.
 * @param newsItems An array of news item information:
 *          - An ID, title and url.
 *          - A shortened excerpt of the contents (to maxlength characters), terminated by "..." if
 *              longer than maxLength.
 *          - A comma-separated string of labels and UNIX timestamp.
 */
data class AppNews(
    @field:Json(name = "appid")
    val appId: String,
    @field:Json(name = "newsitems")
    val newsItems: List<NewsItem>?,
    @field:Json(name = "count")
    val count: Int
)

/**
 * @param author can be empty.
 * @param contents is returned as attributed text.
 */
@Entity(
    tableName = "news",
    primaryKeys = ["gid", "appId"]
)
data class NewsItem(
    val gid: String,
    val title: String,
    val url: String,
    @field:Json(name = "is_external_url")
    val isExternalUrl: Boolean,
    val author: String,
    val contents: String,
    @field:Json(name = "feedlabel")
    val feedLabel: String,
    val date: Long,
    @field:Json(name = "feedname")
    val feedName: String,
    @field:Json(name = "feed_type")
    val feedType: String,
    @field:Json(name = "appid")
    val appId: String
)