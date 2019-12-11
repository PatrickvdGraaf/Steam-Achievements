package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.text.method.LinkMovementMethod
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.api.response.news.NewsItem
import com.crepetete.steamachievements.util.StringUtils
import com.crepetete.steamachievements.util.extensions.setAttributedText
import kotlinx.android.synthetic.main.view_holder_news.view.*

/**
 * @author: Patrick van de Graaf.
 * @date: Wed 11 Dec, 2019; 13:26.
 */
class NewsViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    fun bind(newsItem: NewsItem?) {
        newsItem?.let { news ->
            view.text_view_news_title.text = StringUtils.limitTextLength(
                view.context,
                news.title,
                600
            )

            if (news.author.isBlank()) {
                view.text_view_news_author.visibility = View.GONE
            } else {
                view.text_view_news_author.setAttributedText(news.author)
                view.text_view_news_author.visibility = View.VISIBLE
            }
            view.text_view_news_author.movementMethod = LinkMovementMethod.getInstance()

            view.text_view_news_text.setAttributedText(news.contents)
            view.text_view_news_text.movementMethod = LinkMovementMethod.getInstance()

            view.image_view_banner_news.visibility = View.GONE
            StringUtils.findImageUrlInText(news.contents)?.let { imageUrl ->
                view.image_view_banner_news.visibility = View.VISIBLE
                Glide.with(view)
                    .load(imageUrl)
                    .into(view.image_view_banner_news)
            }
        }
    }
}