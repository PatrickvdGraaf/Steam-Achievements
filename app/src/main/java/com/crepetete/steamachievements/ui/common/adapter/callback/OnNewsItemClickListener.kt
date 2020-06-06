package com.crepetete.steamachievements.ui.common.adapter.callback

import com.crepetete.steamachievements.api.response.news.NewsItem

/**
 *
 * Listener for [NewsViewHolder]s or any other ViewHolder/adapter that can request an action upon
 * selecting a [NewsItem].
 *
 * @author: Patrick van de Graaf.
 * @date: Sat 15 Feb, 2020; 15:12.
 */
interface OnNewsItemClickListener {
    fun onNewsItemSelected(item: NewsItem)
}