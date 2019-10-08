package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
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
import com.crepetete.steamachievements.ui.common.adapter.sorting.Order
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameData
import timber.log.Timber
import kotlin.math.abs

/**
 * Created at 19 January, 2019.
 */
class GameViewHolder(private val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root) {

    private var palette: Palette? = null

    fun bind(game: Game?) {
        if (game != null) {
            val dataItem = GameData(game)
            binding.gameData = dataItem

            with(binding.achievementsTextView) {
                compoundDrawablePadding = 16
                setCompoundDrawablesWithIntrinsicBounds(
                    if (dataItem.isCompleted()) R.drawable.ic_completed_24dp else 0, 0, 0, 0)
            }

            binding.progressBar.progress = dataItem.getPercentageCompleted().toInt()

            // Set RecyclerView adapter.
            val achievements = game.achievements
            val latestAchievements = achievements.sortedWith(Order.LatestAchievedOrder())
                .take(10)

            if (latestAchievements.isEmpty()) {
                binding.achievementContainer.visibility = View.GONE
            } else {
                binding.achievementContainer.visibility = View.VISIBLE
                latestAchievements.forEachIndexed { index, achievement ->
                    val view = when (index) {
                        0 -> binding.achievement1
                        1 -> binding.achievement2
                        2 -> binding.achievement3
                        3 -> binding.achievement4
                        4 -> binding.achievement5
                        5 -> binding.achievement6
                        6 -> binding.achievement7
                        else -> binding.achievement8
                    }

                    Glide.with(binding.root.context)
                        .load(achievement.getActualIconUrl())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?,
                                                      model: Any?,
                                                      target: Target<Drawable>?,
                                                      isFirstResource: Boolean): Boolean {
                                view.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?,
                                                         model: Any?,
                                                         target: Target<Drawable>?,
                                                         dataSource: DataSource?,
                                                         isFirstResource: Boolean): Boolean {
                                view.visibility = View.VISIBLE
                                return false
                            }

                        })
                        .into(view)
                }
            }

            Glide.with(binding.root.context)
                .asBitmap()
                .load(dataItem.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(resource: Bitmap?,
                                                 model: Any?,
                                                 target: Target<Bitmap>?,
                                                 dataSource: DataSource?,
                                                 isFirstResource: Boolean): Boolean {
                        if (resource != null) {
                            Palette.from(resource).generate { newPalette ->
                                palette = newPalette

                                val darkMuted = palette?.darkMutedSwatch?.rgb
                                val darkVibrant = palette?.darkVibrantSwatch?.rgb
                                val muted = palette?.mutedSwatch?.rgb
                                val lightMuted = palette?.lightMutedSwatch?.rgb
                                val dominant = palette?.dominantSwatch?.rgb
                                if (darkMuted != null) {
                                    binding.gameCard.setCardBackgroundColor(darkMuted)
                                    binding.mainCard.setCardBackgroundColor(darkMuted)
                                } else if (muted != null) {
                                    binding.gameCard.setCardBackgroundColor(muted)
                                    binding.mainCard.setCardBackgroundColor(muted)
                                }

                                when {
                                    darkVibrant != null -> {
                                        binding.nameTextView.setBackgroundColor(darkVibrant)
                                        binding.achievementContainer.setBackgroundColor(darkVibrant)
                                    }
                                    lightMuted != null -> {
                                        binding.nameTextView.setBackgroundColor(lightMuted)
                                        binding.achievementContainer.setBackgroundColor(lightMuted)
                                    }
                                    muted != null -> {
                                        binding.nameTextView.setBackgroundColor(muted)
                                        binding.achievementContainer.setBackgroundColor(muted)
                                    }
                                    dominant != null -> {
                                        binding.nameTextView.setBackgroundColor(dominant)
                                        binding.achievementContainer.setBackgroundColor(dominant)
                                    }
                                }
                            }
                        }
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        Timber.w(e, "Error while loading image from url: ${dataItem.getImageUrl()}.")
                        return false
                    }
                })
                .into(binding.gameBanner)
        }
    }

    fun getPalette(): Palette? = palette

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
                duration = (abs(mTo - mFrom) * (animationDuration / view.max)).toLong()
            }

            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val value = mFrom + (mTo - mFrom) * interpolatedTime
                view.progress = value.toInt()
            }
        })
    }
}