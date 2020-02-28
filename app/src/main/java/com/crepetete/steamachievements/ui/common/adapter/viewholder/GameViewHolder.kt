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
import com.crepetete.steamachievements.databinding.ViewHolderGameBinding
import com.crepetete.steamachievements.ui.common.adapter.callback.ColorListener
import com.crepetete.steamachievements.ui.common.adapter.sorting.Order
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameData
import timber.log.Timber

/**
 * Created at 19 January, 2019.
 */
class GameViewHolder(
    private val binding: ViewHolderGameBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(game: Game?, colorListener: ColorListener) {
        if (game != null) {
            val dataItem = GameData(game)
            binding.gameData = dataItem

            binding.imageViewAchievedFlag.visibility = if (dataItem.isCompleted()) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.imageViewRecentlyPlayed.visibility =
                if (dataItem.getRecentPlaytimeString().isEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            binding.progressBar.progress = dataItem.getPercentageCompleted().toInt()

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

        binding.pulsator.start()
        Glide.with(binding.imageViewGameBanner)
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

                    binding.imageViewGameBanner.setImageDrawable(null)

                    binding.pulsator.stop()
                    binding.loaderButton.setImageDrawable(
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
                    binding.pulsator.stop()
                    binding.pulsator.visibility = View.GONE

                    resource?.let { bitmap ->
                        Palette.from(bitmap).generate { palette ->
                            val backgroundColor =
                                palette?.darkMutedSwatch?.rgb ?: ContextCompat.getColor(
                                    binding.root.context,
                                    R.color.colorPrimaryDark
                                )
                            binding.cardViewGame.setCardBackgroundColor(backgroundColor)

                            colorListener.onPrimaryGameColorCreated(game, backgroundColor)
                        }
                    }

                    return false
                }
            })
            .into(binding.imageViewGameBanner)
    }

    /**
     * Loads images in the Achievements row. It tries to get the 8 most recently achieved
     * achievements and fills the rest with achievements in the order we fetched them from the
     * backend.
     */
    private fun setAchievementsImages(achievements: List<Achievement>) {
        // Empty all ImageViews to prevent wrong images appearing when the user scrolls fast.
        binding.achievement1.setImageDrawable(null)
        binding.achievement2.setImageDrawable(null)
        binding.achievement3.setImageDrawable(null)
        binding.achievement4.setImageDrawable(null)
        binding.achievement5.setImageDrawable(null)
        binding.achievement6.setImageDrawable(null)
        binding.achievement7.setImageDrawable(null)
        binding.achievement8.setImageDrawable(null)

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
                0 -> binding.achievement1
                1 -> binding.achievement2
                2 -> binding.achievement3
                3 -> binding.achievement4
                4 -> binding.achievement5
                5 -> binding.achievement6
                6 -> binding.achievement7
                7 -> binding.achievement8
                else -> null
            }

            view?.let { imageView ->
                Glide.with(imageView)
                    .load(achievement.getActualIconUrl())
                    .placeholder(R.drawable.ic_image_loading)
                    .error(R.drawable.ic_image_failed)
                    .override(
                        binding.root.context.resources
                            .getDimensionPixelSize(R.dimen.size_achievement_small)
                    )
                    .into(imageView)
            }
        }
    }
}