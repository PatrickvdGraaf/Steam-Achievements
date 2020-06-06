package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.api.response.news.NewsItem
import com.crepetete.steamachievements.ui.common.adapter.callback.OnNewsItemClickListener
import com.crepetete.steamachievements.util.Constants
import com.crepetete.steamachievements.util.StringUtils
import com.crepetete.steamachievements.util.extensions.setAttributedText
import kotlinx.android.synthetic.main.view_holder_news.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Code for setting up a News Item ListItem.
 *
 * @author: Patrick van de Graaf.
 * @date: Wed 11 Dec, 2019; 13:26.
 */
class NewsViewHolder(
    private val view: View,
    private val newsSelectionListener: OnNewsItemClickListener
) : RecyclerView.ViewHolder(view) {

    /**
     * Uses a [newsItem] to set up the [view].
     */
    fun bind(newsItem: NewsItem?) {
        newsItem?.let { news ->
            val textViewTitle = view.text_view_news_title
            val textViewAuthor = view.text_view_news_author
            val textViewContent = view.text_view_news_text
            val textViewDate = view.text_view_news_date
            val imageViewBanner = view.image_view_banner_news

            textViewTitle.text = news.title

            if (news.author.isBlank()) {
                textViewAuthor.visibility = View.GONE
            } else {
                textViewAuthor.setAttributedText(news.author)
                textViewAuthor.visibility = View.VISIBLE
                textViewAuthor.movementMethod = LinkMovementMethod.getInstance()
            }

            textViewContent.text = StringUtils.limitTextLength(
                view.context,
                news.contents,
                600,
                View.OnClickListener { newsSelectionListener.onNewsItemSelected(news) }
            )
            textViewContent.movementMethod = LinkMovementMethod.getInstance()

            val pattern = "EEEE HH:mm dd-MM-yyyy"
            val calendarSteam = Constants.steamReleaseCalendar
            val reportDate = Calendar.getInstance()

            reportDate.time = Date(news.date * 1000)

            if (reportDate.after(calendarSteam)) {
                textViewDate.text = SimpleDateFormat(pattern, Locale.getDefault())
                    .format(reportDate.time)
            }

            loadImageInto(imageViewBanner, StringUtils.findImageUrlInText(news.contents))
        }
    }

    private fun loadImageInto(imageView: ImageView, url: String?) {
        if (url == null) {
            imageView.visibility = View.GONE
        } else {
            Glide.with(imageView)
                .load(url)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        imageView.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        imageView.visibility = View.VISIBLE
                        return false
                    }
                })
                .into(imageView)
        }
    }
}