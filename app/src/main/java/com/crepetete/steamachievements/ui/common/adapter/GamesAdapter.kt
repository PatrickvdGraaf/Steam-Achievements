package com.crepetete.steamachievements.ui.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.databinding.ItemGameBinding
import com.crepetete.steamachievements.ui.common.adapter.diffutil.GamesDiffCallback
import com.crepetete.steamachievements.ui.common.adapter.filter.GameFilter
import com.crepetete.steamachievements.ui.common.adapter.filter.GameFilterListener
import com.crepetete.steamachievements.ui.common.adapter.viewholder.GameViewHolder
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.util.extensions.sort
import com.crepetete.steamachievements.vo.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Adapter that shows Games in a (vertical) List.
 */
class GamesAdapter(var listener: GamesAdapterCallback) : RecyclerView.Adapter<GameViewHolder>(), Filterable, GameFilterListener {
    private var items = listOf<Game>()
    private var filteredItems = listOf<Game>()
    private val filter = GameFilter(items, this)

    private var sortMethod = SortingType.PLAYTIME

    private var query: String? = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = GameViewHolder(binding)

        binding.root.setOnClickListener {
            listener.onGameClicked(filteredItems[viewHolder.adapterPosition], binding.gameBanner, binding.background, binding.gameBanner, viewHolder.getPalette())
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
            val game = filteredItems[position]
            holder.bind(game)
            holder.itemView.visibility = View.VISIBLE

            listener.updateAchievementsForGame(game.getAppId().toString())
        } catch (e: IndexOutOfBoundsException) {
            Timber.e(e, "Could not display GameInfo. Invalid index $position on filteredItems with size $itemCount.")
            holder.itemView.visibility = View.GONE
        }
    }

    /**
     * Set the games list shown in the RecyclerView attached to this adapter.
     * Sorts the list with the current sorting method before submitting.
     */
    fun updateGames(games: List<Game>?) {
        games?.let {
            filter.updateGames(games)

            // If we're not currently showing a search result, reset displayed items to unfiltered data.
            if (query.isNullOrBlank()) {
                val diffResult = sortData(games)
                diffResult.dispatchUpdatesTo(this@GamesAdapter)

            }
        }
    }

    /**
     * Update the search query. Displayed games must have the query String in their name.
     */
    fun setQuery(query: String?) {
        if (!items.isNullOrEmpty()) {
            this.query = query
            filter.filter(query)
        }
    }

    /**
     * Update shown data after a new search query was processed.
     */
    override fun updateFilteredData(data: List<Game>) {
        CoroutineScope(Main).launch {
            val diffResult = sortData(data)
            diffResult.dispatchUpdatesTo(this@GamesAdapter)
        }
    }

    private fun sortData(data: List<Game>): DiffUtil.DiffResult {
        val sortedData = data.sort(sortMethod)
        filteredItems = sortedData
        return DiffUtil.calculateDiff(GamesDiffCallback(filteredItems, sortedData))
    }

    /**
     * Required method from the [Filterable] interface.
     */
    override fun getFilter(): Filter {
        return filter
    }

    override fun getItemCount() = filteredItems.size

    interface GamesAdapterCallback {
        fun onGameClicked(game: Game, imageView: ImageView, background: View, title: View, palette: Palette?)
        fun onPrimaryGameColorCreated(game: Game, rgb: Int)
        fun updateAchievementsForGame(appId: String)
    }
}