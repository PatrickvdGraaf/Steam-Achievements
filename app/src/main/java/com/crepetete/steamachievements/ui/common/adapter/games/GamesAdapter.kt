package com.crepetete.steamachievements.ui.common.adapter.games

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.ItemGameBinding
import com.crepetete.steamachievements.ui.common.adapter.DataBoundListAdapter
import com.crepetete.steamachievements.util.extensions.animateToPercentage
import com.crepetete.steamachievements.util.extensions.setBackgroundColorAnimated
import com.crepetete.steamachievements.util.extensions.setCompletedFlag
import com.crepetete.steamachievements.util.extensions.sort
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.GameWithAchievements

/**
 * Adapter that shows Games in a (vertical) List.
 */
class GamesAdapter(
    appExecutors: AppExecutors,
    private val dataBindingComponent: DataBindingComponent
) : DataBoundListAdapter<GameWithAchievements, ItemGameBinding>(
    appExecutors,
    object : DiffUtil.ItemCallback<GameWithAchievements>() {
        override fun areItemsTheSame(
            oldItem: GameWithAchievements,
            newItem: GameWithAchievements
        ) = oldItem.getAppId() == newItem.getAppId()

        override fun areContentsTheSame(
            oldGame: GameWithAchievements,
            newGame: GameWithAchievements
        ) = oldGame.getName() == newGame.getName()
            && oldGame.getPlaytime() == newGame.getPlaytime()
            && oldGame.getRecentPlaytime() == newGame.getRecentPlaytime()
            && oldGame.getIconUrl() == newGame.getIconUrl()
            && oldGame.getBannerUrl() == newGame.getBannerUrl()
            && oldGame.getAmountOfAchievements() == newGame.getAmountOfAchievements()
            && oldGame.getAchievedAchievements() == newGame.getAchievedAchievements()
    }
) {

    private var items = listOf<GameWithAchievements>()

    private var sortMethod = SortingType.PLAYTIME

    var listener: OnGameBindListener? = null

    override fun createBinding(parent: ViewGroup): ItemGameBinding {
        return DataBindingUtil.inflate<ItemGameBinding>(LayoutInflater.from(parent.context),
            R.layout.item_game, parent, false, dataBindingComponent)
    }

    override fun bind(binding: ItemGameBinding, item: GameWithAchievements) {
        binding.gameWithAch = item
        val view = binding.root

        view.setOnClickListener {
            listener?.onGameClicked(item.getAppId(), binding.gameBanner)
        }

        binding.totalPlayedTextView.text = item.getTotalPlayTimeString(view.context)

        if (item.getRecentPlaytime() > 0) {
            binding.recentlyPlayedTextView.text = item.getRecentPlaytimeString(view.context)
        }

        val percentage = item.getPercentageCompleted().toInt()
        binding.progressBar.animateToPercentage(percentage)

        binding.achievementsTextView.text = item.getAchievementsText()
        binding.achievementsTextView.setCompletedFlag(item.isCompleted())

        Glide.with(view.context)
            .load(item.getFullLogoUrl())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : SimpleTarget<Drawable>() {
                /**
                 * The method that will be called when the resource load has finished.
                 *
                 * @param resource the loaded resource.
                 */
                override fun onResourceReady(resource: Drawable,
                                             transition: Transition<in Drawable>?) {
                    binding.gameBanner.setImageDrawable(resource)
                    if (item.getPrimaryColor() == 0 && resource is BitmapDrawable) {
                        animateBackground(item, binding.content, resource.bitmap)
                    } else {
                        binding.content.setBackgroundColor(item.getPrimaryColor())
                    }
                }
            })

        listener?.onGameBoundInAdapter(item.getAppId())
    }

    /**
     * Set the games list shown in the RecyclerView attached to this adapter.
     * Sorts the list with the current sorting method before submitting.
     */
    fun setGames(games: List<GameWithAchievements>) {
        items = games.sort(sortMethod)
        submitList(items)
    }

    /**
     * Update the achievements in the existing list of games.
     */
    fun setAchievements(achievements: List<Achievement>) {
        //        val gamesWithAchievements = items.toMutableList()
        //        gamesWithAchievements.forEach { game ->
        //            game.setAchievements(achievements.filter { achievement -> achievement.getAppId == game.getAppId })
        //        }
        //
        //        submitList(gamesWithAchievements)
    }

    fun setAchievements(appId: String, achievements: List<Achievement>) {
        val gamesWithAchievements = items.toMutableList()
        gamesWithAchievements.filter { it.getAppId() == appId }.forEach {
            if (it.achievements != achievements) {
                it.achievements
            }
        }

        submitList(gamesWithAchievements)
    }

    private fun animateBackground(game: GameWithAchievements, view: View, bitmap: Bitmap) {
        Palette.from(bitmap).generate {
            val vibrantRgb = it?.darkVibrantSwatch?.rgb
            val mutedRgb = it?.darkMutedSwatch?.rgb
            val defaultBackgroundColor = ContextCompat.getColor(view.context,
                R.color.colorGameViewHolderTitleBackground)

            val rgb = when {
                mutedRgb != null -> mutedRgb
                vibrantRgb != null -> vibrantRgb
                else -> defaultBackgroundColor
            }

            view.setBackgroundColorAnimated(defaultBackgroundColor, rgb)

            if (rgb != defaultBackgroundColor) {
                listener?.onPrimaryGameColorCreated(game.getAppId(), rgb)
            }
        }
    }

    interface OnGameBindListener {
        fun onGameBoundInAdapter(appId: String)
        fun onGameClicked(appId: String, imageView: ImageView)
        fun onPrimaryGameColorCreated(appId: String, rgb: Int)
    }
}