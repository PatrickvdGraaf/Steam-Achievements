package com.crepetete.steamachievements.ui.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.databinding.ItemGameBinding
import com.crepetete.steamachievements.ui.common.adapter.diffutil.GamesDiffCallback
import com.crepetete.steamachievements.ui.common.adapter.filter.GameFilter
import com.crepetete.steamachievements.ui.common.adapter.filter.GameFilterListener
import com.crepetete.steamachievements.ui.common.adapter.viewholder.GameViewHolder
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.util.extensions.sort
import com.crepetete.steamachievements.vo.GameWithAchievements
import timber.log.Timber

/**
 * Adapter that shows Games in a (vertical) List.
 */
class GamesAdapter : RecyclerView.Adapter<GameViewHolder>(), Filterable, GameFilterListener {
    private var items = listOf<GameWithAchievements>()
    private var filteredItems = listOf<GameWithAchievements>()
    private val filter = GameFilter(items, this)

    private var sortMethod = SortingType.PLAYTIME

    var listener: OnGameClickListener? = null

    private var query: String? = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = GameViewHolder(binding)

        binding.root.setOnClickListener {
            listener?.onGameClicked(filteredItems[viewHolder.adapterPosition], binding.gameBanner, binding.background, binding.gameBanner)
        }

        return viewHolder
    }

    /**
     * Pass an empty list to [onBindViewHolder].
     * Can't override the default value for the overridden method, so we pass it here.
     */
    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        onBindViewHolder(holder, position, listOf())
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int, payLoads: List<Any>) {
        try {
            holder.bind(filteredItems[position])
            holder.itemView.visibility = View.VISIBLE
        } catch (e: IndexOutOfBoundsException) {
            Timber.e(e, "Could not display Game in RecyclerView. Invalid index $position on filteredItems with size $itemCount.")
            holder.itemView.visibility = View.GONE
        }
    }

    /**
     * Set the games list shown in the RecyclerView attached to this adapter.
     * Sorts the list with the current sorting method before submitting.
     */
    fun updateGames(games: List<GameWithAchievements>?) {
        items = games.sort(sortMethod)
        filter.updateGames(items)

        // If we're not currently showing a search result, reset displayed items to unfiltered data.
        if (query.isNullOrBlank()) {
            val diffResult = DiffUtil.calculateDiff(GamesDiffCallback(filteredItems, items))
            diffResult.dispatchUpdatesTo(this)
            filteredItems = items
        }
    }

    /**
     * Update the search query. Displayed games must have the query String in their name.
     */
    fun setQuery(query: String?) {
        this.query = query
        filter.filter(query)
    }

    /**
     * Update shown data after a new search query was processed.
     */
    override fun updateFilteredData(data: List<GameWithAchievements>) {
        val sortedData = data.sort(sortMethod)
        val diffResult = DiffUtil.calculateDiff(GamesDiffCallback(filteredItems, sortedData))

        diffResult.dispatchUpdatesTo(this)
        filteredItems = sortedData
    }

    /**
     * Required method from the [Filterable] interface.
     */
    override fun getFilter(): Filter {
        return filter
    }

    override fun getItemCount() = filteredItems.size

    interface OnGameClickListener {
        fun onGameClicked(game: GameWithAchievements, imageView: ImageView, background: View, title: View)
        fun onPrimaryGameColorCreated(game: GameWithAchievements, rgb: Int)
    }
}