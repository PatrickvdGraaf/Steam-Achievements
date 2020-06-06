package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.graphics.Bitmap
import android.view.View
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.ui.common.adapter.callback.ColorListener
import com.crepetete.steamachievements.ui.common.adapter.sorting.Order
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameData
import kotlinx.android.synthetic.main.view_holder_game.view.*
import timber.log.Timber

/**
 * Created at 19 January, 2019.
 */
class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    fun bind(game: Game?, colorListener: ColorListener) {
        if (game != null) {
            val dataItem = GameData(game)

            itemView.imageViewAchievedFlag.visibility = if (dataItem.isCompleted()) {
                View.VISIBLE
            } else {
                View.GONE
            }

            itemView.totalPlayedTextView.text = dataItem.getTotalPlayTimeString()
            itemView.textViewRecentlyPlayed.text = dataItem.getRecentPlaytimeString()

            itemView.imageViewRecentlyPlayed.visibility =
                if (dataItem.getRecentPlaytimeString().isEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            itemView.progressBar.progress = dataItem.getPercentageCompleted().toInt()

            setGameBanner(game, colorListener)
            setAchievementsImages(dataItem.getAchievements())
        }
    }

    /**
     * While setting the image banner, we also attempt to generate a fitting background color for
     * the CardView based on the colors in the banner image. This is done async using [Palette] when
     * the image is successfully loaded.
     */
    private fun setGameBanner(game: Game, colorListener: ColorListener) {
        val url = game.getBannerUrl()

        itemView.pulsator.start()
        Glide.with(itemView.imageViewGameBanner)
            .asBitmap()
            .load(url)
            .listener(object : RequestListener<Bitmap> {
                /*
                 * On Error, set the image in the banner to null to prevent cache issues in the
                 * list. Then stop the loader and replace the loader image with an image that
                 * indicates that the banner could not be fetched.
                 */
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Timber.w(e, "Error while loading achievement image from url: $url.")

                    itemView.imageViewGameBanner.setImageDrawable(null)

                    itemView.pulsator.stop()
                    itemView.loaderButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.ic_image_failed
                        )
                    )

                    return false
                }

                /*
                 * On Success, set the Card Background to the Dark Muted Swatch, or
                 * colorPrimaryDark if Swatch generation fails.
                 */
                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    itemView.pulsator.stop()
                    itemView.pulsator.visibility = View.GONE

                    resource?.let { bitmap ->
                        Palette.from(bitmap).generate { palette ->
                            val backgroundColor =
                                palette?.darkMutedSwatch?.rgb ?: ContextCompat.getColor(
                                    itemView.context,
                                    R.color.colorPrimaryDark
                                )
                            itemView.cardViewGame.setCardBackgroundColor(backgroundColor)

                            colorListener.onPrimaryGameColorCreated(game, backgroundColor)
                        }
                    }

                    return false
                }
            })
            .into(itemView.imageViewGameBanner)
    }

    /**
     * Loads images in the Achievements row. It tries to get the 8 most recently achieved
     * achievements and fills the rest with achievements in the order we fetched them from the
     * backend.
     */
    private fun setAchievementsImages(achievements: List<Achievement>) {
        // Empty all ImageViews to prevent wrong images appearing when the user scrolls fast.
        itemView.achievement1.setImageDrawable(null)
        itemView.achievement2.setImageDrawable(null)
        itemView.achievement3.setImageDrawable(null)
        itemView.achievement4.setImageDrawable(null)
        itemView.achievement5.setImageDrawable(null)
        itemView.achievement6.setImageDrawable(null)
        itemView.achievement7.setImageDrawable(null)
        itemView.achievement8.setImageDrawable(null)

        // Take the last 8 unlocked achievements and add 8 more to fill all view in case the player
        // doesn't have 8 unlocked achievements.
        val showAchievements = achievements
            .filter { achievement -> achievement.achieved }
            .sortedWith(Order.LatestAchievedOrder())
            .take(8)
            .toMutableList()
        showAchievements.addAll(achievements.take(8))

        showAchievements.forEachIndexed { index, achievement ->
            val view = when (index) {
                0 -> itemView.achievement1
                1 -> itemView.achievement2
                2 -> itemView.achievement3
                3 -> itemView.achievement4
                4 -> itemView.achievement5
                5 -> itemView.achievement6
                6 -> itemView.achievement7
                7 -> itemView.achievement8
                else -> null
            }

            view?.let { imageView ->
                Glide.with(imageView)
                    .load(achievement.getActualIconUrl())
                    .placeholder(R.drawable.ic_image_loading)
                    .error(R.drawable.ic_image_failed)
                    .override(itemView.context.resources.getDimensionPixelSize(R.dimen.size_achievement_small))
                    .into(imageView)
            }
        }
    }
}