package com.crepetete.steamachievements.ui.fragment.achievement

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.CardView
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.utils.GlideApp
import com.crepetete.steamachievements.utils.bind
import com.crepetete.steamachievements.utils.setBackgroundColorAnimated

/**
 * Dialog displaying all info for an Achievement.
 */
class AchievementDialog(context: Context, private val achievement: Achievement) : Dialog(context) {
    private val iconView by bind<ImageView>(R.id.achievement_icon_imageview)
    private val cardView by bind<CardView>(R.id.achievement_cardview)
    private val nameView by bind<TextView>(R.id.achievement_name_textview)
    private val dateView by bind<TextView>(R.id.achievement_date_textview)
    private val descView by bind<TextView>(R.id.achievement_desc_textview)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_achievement)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        nameView.text = achievement.displayName
        dateView.text = achievement.getDateStringNoBreak()

        val desc = achievement.description
        if (desc.isNullOrBlank()) {
            descView.visibility = View.GONE
        } else {
            descView.text = achievement.description
            descView.visibility = View.VISIBLE
        }

        setIconAndColors(achievement.iconUrl)
    }

    private fun setIconAndColors(iconUrl: String) {
        GlideApp.with(context)
                .load(iconUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?,
                                              model: Any?,
                                              target: Target<Drawable>?,
                                              isFirstResource: Boolean): Boolean {
                        // TODO
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?,
                                                 model: Any?,
                                                 target: Target<Drawable>?,
                                                 dataSource: DataSource?,
                                                 isFirstResource: Boolean): Boolean {
                        if (resource != null && resource is BitmapDrawable) {
                            Palette.from(resource.bitmap).generate {
                                val darkVibrantSwatch = it?.darkVibrantSwatch
                                val darkMutedSwatch = it?.darkMutedSwatch

                                if (darkMutedSwatch != null) {
                                    cardView.setBackgroundColorAnimated(
                                            ContextCompat.getColor(context, R.color.colorPrimary),
                                            darkMutedSwatch.rgb, 300)
                                    iconView.setBackgroundColorAnimated(
                                            ContextCompat.getColor(context,
                                                    R.color.colorPrimaryDark),
                                            darkMutedSwatch.rgb)

//                                    nameView.setTextColor(darkVibrantSwatch.titleTextColor)
                                    dateView.setTextColor(darkMutedSwatch.bodyTextColor)
                                } else if (darkVibrantSwatch != null) {
                                    cardView.setBackgroundColorAnimated(
                                            ContextCompat.getColor(context, R.color.colorPrimary),
                                            darkVibrantSwatch.rgb, 300)
                                    iconView.setBackgroundColorAnimated(
                                            ContextCompat.getColor(context,
                                                    R.color.colorPrimaryDark),
                                            darkVibrantSwatch.rgb)

//                                    nameView.setTextColor(darkVibrantSwatch.titleTextColor)
                                    dateView.setTextColor(darkVibrantSwatch.bodyTextColor)
                                }
                            }
                        }
                        return false
                    }

                }).into(iconView)
    }
}