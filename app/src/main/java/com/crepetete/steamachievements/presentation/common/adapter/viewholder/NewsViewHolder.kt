package com.crepetete.steamachievements.presentation.common.adapter.viewholder

import android.graphics.drawable.Drawable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.presentation.activity.image.ImageFullScreenActivity
import com.crepetete.steamachievements.presentation.common.adapter.callback.OnNewsItemClickListener
import com.crepetete.steamachievements.util.Constants
import com.crepetete.steamachievements.util.StringUtils
import kotlinx.android.synthetic.main.view_holder_news.view.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    private companion object {
        const val MAX_LENGTH_ARTICLE = 600
    }

    /**
     * Uses a [newsItem] to set up the [view].
     */
    fun bind(newsItem: NewsItem?) {
        newsItem?.let { news ->
            with(view) {
                textViewNewsTitle.text = news.title

                // Show author name, email, tag text, or hide the view.
                if (news.author.isBlank()) {
                    textViewNewsAuthor.visibility = View.GONE
                } else {
                    textViewNewsAuthor.text = Html.fromHtml(news.author, Html.FROM_HTML_MODE_LEGACY)
                    textViewNewsAuthor.visibility = View.VISIBLE
                    textViewNewsAuthor.movementMethod = LinkMovementMethod.getInstance()
                }

                // Parse a readable representation for the upload date. 
                val pattern = "EEE HH:mm dd-MM-yyyy"
                val calendarSteam = Constants.steamReleaseCalendar
                val reportDate = Calendar.getInstance()
                reportDate.time = Date(news.date * 1000)

                if (reportDate.after(calendarSteam)) {
                    textViewNewsDate.text = SimpleDateFormat(pattern, Locale.getDefault())
                        .format(reportDate.time)
                }

                // Show the 'more' button and separators accordingly.
                val isTextCapped = news.contents.length > MAX_LENGTH_ARTICLE

                buttonShowMore.visibility = if (isTextCapped) View.VISIBLE else View.GONE
                viewSeparatorLeft.visibility = if (isTextCapped) View.VISIBLE else View.GONE
                viewSeparatorRight.visibility = if (isTextCapped) View.VISIBLE else View.GONE
                viewSeparatorFull.visibility = if (isTextCapped) View.GONE else View.VISIBLE

                buttonShowMore.setOnClickListener {
                    newsSelectionListener.onNewsItemSelected(news)
                }

                val imageUrl = StringUtils.findImageUrlInText(news.contents)
                val text = if (imageUrl != null) {
                    news.contents.replace(imageUrl, "")
                } else news.contents

                // Show the news article main text, capped at MAX_LENGTH_ARTICLE characters.
                textViewNewsText.movementMethod = LinkMovementMethod.getInstance()
                textViewNewsText.text =
                    Html.fromHtml(
                        StringUtils.limitTextLength(text, MAX_LENGTH_ARTICLE),
                        Html.FROM_HTML_MODE_LEGACY
                    )

                // Show a banner image, if available.
                loadImageInto(
                    imageViewBannerNews,
                    imageUrl
                )

                imageUrl?.let { url ->
                    imageViewBannerNews.setOnClickListener {
                        view.context.startActivity(
                            ImageFullScreenActivity.getIntent(
                                view.context,
                                url
                            )
                        )
                    }
                }
            }
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