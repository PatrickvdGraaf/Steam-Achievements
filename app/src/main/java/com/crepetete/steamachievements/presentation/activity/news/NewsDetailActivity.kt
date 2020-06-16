package com.crepetete.steamachievements.presentation.activity.news

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.util.Constants
import com.crepetete.steamachievements.util.StringUtils
import kotlinx.android.synthetic.main.activity_news_detail.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NewsDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_BUNDLE = "EXTRA_BUNDLE"
        private const val EXTRA_NEWS_ITEM = "EXTRA_NEWS_ITEM"
        fun getIntent(context: Context, newsItem: NewsItem): Intent {
            return Intent(context, NewsDetailActivity::class.java).apply {
                val bundle = Bundle()
                bundle.putParcelable(EXTRA_NEWS_ITEM, newsItem)
                putExtra(EXTRA_NEWS_ITEM, newsItem)
                putExtra(EXTRA_BUNDLE, bundle)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_detail)

        intent?.getBundleExtra(EXTRA_BUNDLE)?.getParcelable<NewsItem?>(EXTRA_NEWS_ITEM)
            ?.let { news ->
                // TODO This data is the same as in the NewsItemViewHolder, maybe merge them into one?
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

                val imageUrl = StringUtils.findImageUrlInText(news.contents)
                val text = if (imageUrl != null) {
                    news.contents.replace(imageUrl, "")
                } else news.contents

                textViewNewsText.text = Html.fromHtml(
                    text
                        .replace("[", "<")
                        .replace("]", ">")
                        .replace("\n", "<br/>"), Html.FROM_HTML_MODE_LEGACY
                )
                textViewNewsText.movementMethod = LinkMovementMethod.getInstance()

                // Show a banner image, if available.
                loadImageInto(
                    imageViewBanner,
                    imageUrl
                )
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
