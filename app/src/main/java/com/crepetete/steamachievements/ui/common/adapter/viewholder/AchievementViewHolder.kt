package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.ui.common.loader.PulsatorLayout
import com.crepetete.steamachievements.vo.Achievement
import timber.log.Timber

class AchievementViewHolder(private val view: View)
    : RecyclerView.ViewHolder(view) {

    private val textViewTitle = view.findViewById<TextView>(R.id.textViewTitle)
    private val pulsator = view.findViewById<PulsatorLayout>(R.id.pulsator)
    val imageView: ImageView = view.findViewById(R.id.imageView)

    private var startingIndex = 0

    fun bind(achievement: Achievement, index: Int) {
        this.startingIndex = index
        textViewTitle.text = achievement.displayName

        val context = view.context
        if (context != null) {

            pulsator.visibility = View.VISIBLE
            pulsator.start()

            Glide.with(context)
                .load(if (achievement.achieved) {
                    achievement.iconUrl
                } else {
                    achievement.iconGrayUrl
                })
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?,
                                              model: Any?,
                                              target: Target<Drawable>?,
                                              isFirstResource: Boolean): Boolean {
                        Timber.w(e, "Error while loading image from url: ${achievement.iconUrl}.")

                        pulsator.stop()

                        return false
                    }

                    override fun onResourceReady(resource: Drawable?,
                                                 model: Any?,
                                                 target: Target<Drawable>?,
                                                 dataSource: DataSource?,
                                                 isFirstResource: Boolean): Boolean {
                        pulsator.stop()
                        pulsator.visibility = View.GONE
                        return false
                    }
                })
                .into(imageView)
        }
    }
}