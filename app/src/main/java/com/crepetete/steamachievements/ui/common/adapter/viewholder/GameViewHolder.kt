package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.graphics.Bitmap
import android.view.View
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.databinding.ViewHolderGameBinding
import com.crepetete.steamachievements.ui.common.adapter.sorting.Order
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameData
import timber.log.Timber

/**
 * Created at 19 January, 2019.
 */
class GameViewHolder(
    private val binding: ViewHolderGameBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(game: Game?) {
        if (game != null) {
            val dataItem = GameData(game)
            binding.gameData = dataItem

            binding.imageViewAchievedFlag.visibility = if (dataItem.isCompleted()) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.progressBar.progress = dataItem.getPercentageCompleted().toInt()

            // Set Achievements images.
            binding.achievement1.setImageDrawable(null)
            binding.achievement2.setImageDrawable(null)
            binding.achievement3.setImageDrawable(null)
            binding.achievement4.setImageDrawable(null)
            binding.achievement5.setImageDrawable(null)
            binding.achievement6.setImageDrawable(null)
            binding.achievement7.setImageDrawable(null)
            binding.achievement8.setImageDrawable(null)

            val achievements = game.achievements
            val latestAchievements = achievements
                .filter { achievement -> achievement.achieved }
                .sortedWith(Order.LatestAchievedOrder())
                .take(10)

            val showAchievements = if (latestAchievements.isEmpty()) {
                achievements
            } else {
                latestAchievements
            }

            showAchievements.forEachIndexed { index, achievement ->
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

                Glide.with(view)
                    .load(achievement.getActualIconUrl())
                    .override(36)
                    .into(view)
            }

            // Set Game Banner.
            Glide.with(binding.imageViewGameBanner)
                .asBitmap()
                .load(dataItem.getImageUrl())
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Timber.w(
                            e,
                            "Error while loading image from url: ${dataItem.getImageUrl()}."
                        )

                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: com.bumptech.glide.load.DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        resource?.let { bitmap ->
                            Palette.from(bitmap).generate { palette ->
                                palette?.darkMutedSwatch?.rgb?.let { rgb ->
                                    binding.cardViewGame.setCardBackgroundColor(rgb)
                                    binding.cardViewGame.setCardBackgroundColor(rgb)
                                }
                            }
                        }

                        return false
                    }
                })
                .into(binding.imageViewGameBanner)
        }
    }
}