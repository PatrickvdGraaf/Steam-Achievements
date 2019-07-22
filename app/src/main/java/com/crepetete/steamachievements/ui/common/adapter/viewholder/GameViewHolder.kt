package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
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
import com.crepetete.steamachievements.ui.common.adapter.sorting.Order
import com.crepetete.steamachievements.vo.GameData
import com.crepetete.steamachievements.vo.GameWithAchievements
import timber.log.Timber

/**
 * Created at 19 January, 2019.
 */
class GameViewHolder(private val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root) {

    private var palette: Palette? = null

    fun bind(game: GameWithAchievements?) {
        if (game != null) {
            val dataItem = GameData(game)
            binding.gameData = dataItem

            with(binding.achievementsTextView) {
                compoundDrawablePadding = 16
                setCompoundDrawablesWithIntrinsicBounds(
                    if (dataItem.isCompleted()) R.drawable.ic_completed_24dp else 0, 0, 0, 0)
            }

            if (dataItem.isCompleted()) {
                binding.imageViewAchievedFlag.visibility = View.VISIBLE
            } else {
                binding.imageViewAchievedFlag.visibility = View.GONE
            }

            binding.progressBar.progress = dataItem.getPercentageCompleted().toInt()

            binding.achievement1.visibility = View.INVISIBLE
            binding.achievement2.visibility = View.INVISIBLE
            binding.achievement3.visibility = View.INVISIBLE
            binding.achievement4.visibility = View.INVISIBLE
            binding.achievement5.visibility = View.INVISIBLE
            binding.achievement6.visibility = View.INVISIBLE
            binding.achievement7.visibility = View.INVISIBLE
            binding.achievement8.visibility = View.INVISIBLE

            // Set RecyclerView adapter.
            val achievements = game.achievements
            val latestAchievements = achievements
                .filter { achievement -> achievement.achieved }
                .sortedWith(Order.LatestAchievedOrder())
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
                                val vibrantRgb = newPalette?.darkVibrantSwatch?.rgb
                                val mutedRgb = newPalette?.darkMutedSwatch?.rgb

                                when {
                                    mutedRgb != null -> mutedRgb
                                    vibrantRgb != null -> vibrantRgb
                                    else -> ContextCompat.getColor(binding.root.context,
                                        R.color.colorGameViewHolderTitleBackground)
                                }.let { color ->
                                    binding.gameCard.setCardBackgroundColor(color)
                                    binding.mainCard.setCardBackgroundColor(color)
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

}