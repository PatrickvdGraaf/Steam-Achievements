package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.os.Build
import android.text.Html
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.api.response.news.NewsItem
import kotlinx.android.synthetic.main.view_holder_news.view.*

/**
 * @author: Patrick van de Graaf.
 * @date: Wed 11 Dec, 2019; 13:26.
 */
class NewsViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(newsItem: NewsItem?) {
        newsItem?.let { news ->
            view.text_view_news_title.text = news.title
            if (news.author.isBlank()) {
                view.text_view_news_author.visibility = View.GONE
            } else {
                view.text_view_news_author.text = news.author
                view.text_view_news_author.visibility = View.VISIBLE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.text_view_news_text.text = Html.fromHtml(
                    news.contents,
                    Html.FROM_HTML_MODE_LEGACY
                )
            } else {
                @Suppress("DEPRECATION")
                view.text_view_news_text.text = Html.fromHtml(news.contents)
            }
        }
    }
}