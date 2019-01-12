package com.crepetete.steamachievements.ui.common.adapter.games

import android.content.Context
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
import com.bumptech.glide.Priority
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.ItemGameBinding
import com.crepetete.steamachievements.ui.common.adapter.DataBoundListAdapter
import com.crepetete.steamachievements.util.extensions.setBackgroundColorAnimated
import com.crepetete.steamachievements.util.extensions.sort
import com.crepetete.steamachievements.vo.GameData
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

    override fun createBinding(parent: ViewGroup) = DataBindingUtil.inflate(
        LayoutInflater.from(parent.context),
        R.layout.item_game,
        parent,
        false,
        dataBindingComponent
    ) as ItemGameBinding

    override fun bind(binding: ItemGameBinding, item: GameWithAchievements) {
        val dataItem = GameData(item)
        binding.gameData = dataItem

        if (item.getPrimaryColor() == 0) {
            updateBackgroundColorFromBanner(binding.root.context, binding.content, dataItem.getImageUrl(), item.getAppId())
        } else {
            binding.content.setBackgroundColor(item.getPrimaryColor())
        }

        binding.root.setOnClickListener {
            listener?.onGameClicked(item.getAppId(), binding.gameBanner)
        }

        listener?.onGameBoundInAdapter(item.getAppId())
    }

    private fun updateBackgroundColorFromBanner(context: Context, view: View, url: String, appId: String) {
        Glide.with(context)
            .load(url)
            .priority(Priority.LOW)
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable,
                                             transition: Transition<in Drawable>?) {
                    if (resource is BitmapDrawable) {
                        Palette.from(resource.bitmap).generate {
                            val vibrantRgb = it?.darkVibrantSwatch?.rgb
                            val mutedRgb = it?.darkMutedSwatch?.rgb
                            val defaultBackgroundColor = ContextCompat.getColor(context,
                                R.color.colorGameViewHolderTitleBackground)

                            val rgb = when {
                                mutedRgb != null -> mutedRgb
                                vibrantRgb != null -> vibrantRgb
                                else -> defaultBackgroundColor
                            }

                            // TODO remove this and let LiveData observers refresh the list.
                            view.setBackgroundColorAnimated(defaultBackgroundColor, rgb)

                            // Listener should update the database, which will trigger LiveData observers,
                            // and the view should reload with the new background color.
                            listener?.onPrimaryGameColorCreated(appId, rgb)
                        }
                    }
                }
            })
    }

    /**
     * Set the games list shown in the RecyclerView attached to this adapter.
     * Sorts the list with the current sorting method before submitting.
     */
    fun setGames(games: List<GameWithAchievements>?) {
        items = games.sort(sortMethod)
        submitList(items)
    }

    interface OnGameBindListener {
        fun onGameBoundInAdapter(appId: String)
        fun onGameClicked(appId: String, imageView: ImageView)
        fun onPrimaryGameColorCreated(appId: String, rgb: Int)
    }
}