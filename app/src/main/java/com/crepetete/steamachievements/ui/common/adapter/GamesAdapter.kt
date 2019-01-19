package com.crepetete.steamachievements.ui.common.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.ItemGameBinding
import com.crepetete.steamachievements.ui.common.adapter.diffutil.GamesDiffCallback
import com.crepetete.steamachievements.ui.common.adapter.viewholder.GameViewHolder
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.util.extensions.sort
import com.crepetete.steamachievements.vo.GameWithAchievements

/**
 * Adapter that shows Games in a (vertical) List.
 */
class GamesAdapter : RecyclerView.Adapter<GameViewHolder>() {

    private var items = listOf<GameWithAchievements>()

    private var sortMethod = SortingType.PLAYTIME

    var listener: OnGameBindListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = GameViewHolder(binding)

        binding.root.setOnClickListener {
            listener?.onGameClicked(items[viewHolder.adapterPosition].getAppId(), binding.gameBanner)
        }

        return viewHolder
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        onBindViewHolder(holder, position, listOf())
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int, payLoads: List<Any>) {
        val game = items[position]
        holder.bind(items[position])
        if (game.getPrimaryColor() == 0) {
            updateBackgroundColorFromBanner(holder.itemView.context, game.getBannerUrl(), game.getAppId())
        }

        listener?.onGameBoundInAdapter(game.getAppId())
    }

    private fun updateBackgroundColorFromBanner(context: Context, url: String, appId: String) {
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
    fun updateGames(games: List<GameWithAchievements>?) {
        val diffResult = DiffUtil.calculateDiff(GamesDiffCallback(items, games.sort(sortMethod)))
        diffResult.dispatchUpdatesTo(this)

        items = games.sort(sortMethod)
    }

    interface OnGameBindListener {
        fun onGameBoundInAdapter(appId: String)
        fun onGameClicked(appId: String, imageView: ImageView)
        fun onPrimaryGameColorCreated(appId: String, rgb: Int)
    }
}