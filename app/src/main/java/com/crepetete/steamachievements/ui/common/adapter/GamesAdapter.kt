package com.crepetete.steamachievements.ui.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
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
            listener?.onGameClicked(items[viewHolder.adapterPosition], binding.gameBanner, binding.background, binding.gameBanner)
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

        listener?.onGameBoundInAdapter(game.getAppId())
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
        fun onGameClicked(game: GameWithAchievements, imageView: ImageView, background: View, title: View)
        fun onPrimaryGameColorCreated(game: GameWithAchievements, rgb: Int)
    }
}