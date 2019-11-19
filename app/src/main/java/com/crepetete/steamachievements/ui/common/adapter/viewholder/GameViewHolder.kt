package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.graphics.Bitmap
import android.view.View
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.decode.DataSource
import coil.request.Request
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.ItemGameBinding
import com.crepetete.steamachievements.ui.common.adapter.sorting.Order
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameData
import timber.log.Timber

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
                    if (dataItem.isCompleted()) R.drawable.ic_completed_24dp else 0, 0, 0, 0
                )
            }

            if (dataItem.isCompleted()) {
                binding.imageViewAchievedFlag.visibility = View.VISIBLE
            } else {
                binding.imageViewAchievedFlag.visibility = View.GONE
            }

            binding.progressBar.progress = dataItem.getPercentageCompleted().toInt()

            // Set RecyclerView adapter.
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

                view.load(achievement.getActualIconUrl()) {
                    listener(object : Request.Listener {
                        override fun onError(data: Any, throwable: Throwable) {
                            super.onError(data, throwable)
                            Timber.w(
                                throwable,
                                "Error while loading image from url: ${achievement.getActualIconUrl()}."
                            )
                        }
                    })
                }
            }

            binding.gameBanner.load(dataItem.getImageUrl()) {
                listener(object : Request.Listener {
                    override fun onError(data: Any, throwable: Throwable) {
                        super.onError(data, throwable)
                        Timber.w(
                            throwable,
                            "Error while loading image from url: ${dataItem.getImageUrl()}."
                        )
                    }

                    override fun onSuccess(data: Any, source: DataSource) {
                        super.onSuccess(data, source)
                        if (data is Bitmap) {
                            Palette.from(data).generate { newPalette ->
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
                    }
                })
            }
        }
    }
}