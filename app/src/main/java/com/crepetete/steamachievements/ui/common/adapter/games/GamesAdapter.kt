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
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.GameItemBinding
import com.crepetete.steamachievements.ui.common.adapter.DataBoundListAdapter
import com.crepetete.steamachievements.util.extensions.animateToPercentage
import com.crepetete.steamachievements.util.extensions.setBackgroundColorAnimated
import com.crepetete.steamachievements.util.extensions.setCompletedFlag
import com.crepetete.steamachievements.util.extensions.sort
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Game

/**
 * Adapter that shows Games in a (vertical) List.
 */
class GamesAdapter(
    appExecutors: AppExecutors,
    private val dataBindingComponent: DataBindingComponent
) : DataBoundListAdapter<Game, GameItemBinding>(
    appExecutors,
    object : DiffUtil.ItemCallback<Game>() {
        override fun areItemsTheSame(oldItem: Game,
                                     newItem: Game): Boolean {
            return oldItem.appId == oldItem.appId
                && oldItem.name == oldItem.name
        }

        override fun areContentsTheSame(oldGame: Game,
                                        newGame: Game): Boolean {
            return oldGame.name == newGame.name
                && oldGame.playTime == newGame.playTime
                && oldGame.recentPlayTime == newGame.recentPlayTime
                && oldGame.iconUrl == newGame.iconUrl
                && oldGame.logoUrl == newGame.logoUrl
                && oldGame.getAmountOfAchievements() == newGame.getAmountOfAchievements()
                && oldGame.getAchievedAchievements() == newGame.getAchievedAchievements()
        }
    }) {

    private var items = listOf<Game>()

    private var sortMethod = SortingType.PLAYTIME

    var listener: OnGameBindListener? = null

    override fun createBinding(parent: ViewGroup): GameItemBinding {
        val binding = DataBindingUtil.inflate<GameItemBinding>(LayoutInflater.from(parent.context),
            R.layout.game_item, parent, false, dataBindingComponent)
        binding.root.setOnClickListener {
            binding.game?.let { game ->
                listener?.onGameClicked(game.appId, binding.gameBanner)
            }
        }

        return binding
    }

    override fun bind(binding: GameItemBinding, item: Game) {
        binding.game = item
        val view = binding.root

        binding.totalPlayedTextView.text = item.getTotalPlayTimeString(view.context)

        if (item.recentPlayTime > 0) {
            binding.recentlyPlayedTextView.text = item.getRecentPlaytimeString(view.context)
            binding.recentlyPlayedTextView.visibility = View.VISIBLE
        } else {
            binding.recentlyPlayedTextView.visibility = View.INVISIBLE
        }

        if (item.hasAchievements()) {
            binding.achievementsTextView.visibility = View.VISIBLE
            val percentage = item.getPercentageCompleted().toInt()
            if (percentage > 0 && binding.progressBar.progress == 0) {
                binding.progressBar.animateToPercentage(percentage)
            } else {
                binding.progressBar.progress = percentage
            }
            binding.achievementsTextView.text = item.getAchievementsText()
            binding.achievementsTextView.setCompletedFlag(item.isCompleted())
        } else if (!item.achievementsWereAdded()) {
            binding.achievementsTextView.visibility = View.VISIBLE
            val context = binding.content.context
            if (context != null) {
                binding.achievementsTextView.text = context.getString(R.string.msg_achievements_loading)
            }
        } else {
            binding.achievementsTextView.visibility = View.GONE
            binding.progressBar.progress = 0
        }

        Glide.with(view.context)
            .load(item.getFullLogoUrl())
            .into(object : SimpleTarget<Drawable>() {
                /**
                 * The method that will be called when the resource load has finished.
                 *
                 * @param resource the loaded resource.
                 */
                override fun onResourceReady(resource: Drawable,
                                             transition: Transition<in Drawable>?) {
                    binding.gameBanner.setImageDrawable(resource)
                    if (item.colorPrimaryDark == 0 && resource is BitmapDrawable) {
                        animateBackground(item, binding.content, resource.bitmap)
                    } else {
                        binding.content.setBackgroundColor(item.colorPrimaryDark)
                    }
                }
            })

        listener?.onGameBoundInAdapter(item.appId)
    }

    /**
     * Set the games list shown in the RecyclerView attached to this adapter.
     * Sorts the list with the current sorting method before submitting.
     */
    fun setGames(games: List<Game>) {
        items = games.sort(sortMethod)
        submitList(items)
    }

    /**
     * Update the achievements in the existing list of games.
     */
    fun setAchievements(achievements: List<Achievement>) {

    }

    fun setAchievements(appId: String, achievements: List<Achievement>) {
        items.filter { it.appId == appId }.forEach {
            if (it.getAchievements() != achievements) {
                it.setAchievements(achievements)
                notifyItemChanged(items.indexOf(it))
            }
        }

//        submitList(items)
    }

    private fun animateBackground(game: Game, view: View, bitmap: Bitmap) {
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
                listener?.onPrimaryGameColorCreated(game.appId, rgb)
            }
        }
    }

    interface OnGameBindListener {
        fun onGameBoundInAdapter(appId: String)
        fun onGameClicked(appId: String, imageView: ImageView)
        fun onPrimaryGameColorCreated(appId: String, rgb: Int)
    }
}