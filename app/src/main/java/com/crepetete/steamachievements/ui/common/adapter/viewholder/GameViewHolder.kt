package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.graphics.Bitmap
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.ItemGameBinding
import com.crepetete.steamachievements.util.extensions.setBackgroundColorAnimated
import com.crepetete.steamachievements.vo.GameData
import com.crepetete.steamachievements.vo.GameWithAchievements
import timber.log.Timber

/**
 * Created at 19 January, 2019.
 */
class GameViewHolder(private val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(game: GameWithAchievements?) {
        if (game != null) {
            val dataItem = GameData(game)
            binding.gameData = dataItem

            with(binding.achievementsTextView) {
                compoundDrawablePadding = 16
                setCompoundDrawablesWithIntrinsicBounds(
                    if (dataItem.isCompleted()) R.drawable.ic_completed_24dp else 0, 0, 0, 0)
            }

            setProgressAnimated(binding.progressBar, dataItem.getPercentageCompleted())

            Glide.with(binding.root.context)
                .asBitmap()
                .load(dataItem.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        Timber.w(e, "Error while loading image from url: ${dataItem.getImageUrl()}.")
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?,
                                                 model: Any?,
                                                 target: Target<Bitmap>?,
                                                 dataSource: DataSource?,
                                                 isFirstResource: Boolean): Boolean {
                        if (resource != null) {
                            Palette.from(resource).generate {
                                val vibrantRgb = it?.darkVibrantSwatch?.rgb
                                val mutedRgb = it?.darkMutedSwatch?.rgb

                                // Listener should update the database, which will trigger Liv=[Data observers,
                                // and the view should reload with the new background color.
                                val defaultBg = ContextCompat.getColor(binding.root.context,
                                    R.color.colorGameViewHolderTitleBackground)

                                binding.background.setBackgroundColorAnimated(defaultBg, when {
                                    mutedRgb != null -> mutedRgb
                                    vibrantRgb != null -> vibrantRgb
                                    else -> defaultBg
                                }, 400)
                            }
                        }
                        return false
                    }

                })
                .into(binding.gameBanner)
        }
    }

    /**
     * Animates the progress from 0 to the given progress param.
     */
    private fun setProgressAnimated(view: ProgressBar, progress: Float) {
        val animationDuration: Long = 1000
        view.progress = 0
        val percentage = progress.toInt()
        view.startAnimation(object : Animation() {
            var mTo = if (percentage < 0) 0 else if (percentage > view.max) view.max else percentage
            var mFrom = progress

            init {
                duration = (Math.abs(mTo - mFrom) * (animationDuration / view.max)).toLong()
            }

            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val value = mFrom + (mTo - mFrom) * interpolatedTime
                view.progress = value.toInt()
            }
        })
    }
}