package com.crepetete.steamachievements.ui.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.databinding.ViewHolderGameBinding
import com.crepetete.steamachievements.ui.common.adapter.callback.ColorListener
import com.crepetete.steamachievements.ui.common.adapter.diffutil.GamesDiffCallback
import com.crepetete.steamachievements.ui.common.adapter.viewholder.GameViewHolder
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.util.extensions.sort
import com.crepetete.steamachievements.vo.Game
import timber.log.Timber
import java.util.*

/**
 * Adapter that shows Games in a (vertical) List.
 */
class GamesAdapter(var listener: GamesAdapterCallback) : RecyclerView.Adapter<GameViewHolder>(),
    Filterable, ColorListener {

    // oldItems is used to calculate to difference compared to the new items using Filter.
    private var oldItems = listOf<Game>()
    private var items = listOf<Game>()

    private var sortMethod = SortingType.PLAYTIME

    private var query: String? = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding =
            ViewHolderGameBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        val viewHolder = GameViewHolder(binding)

        binding.root.setOnClickListener {
            listener.onGameClicked(
                items[viewHolder.adapterPosition],
                binding.imageViewGameBanner,
                binding.imageViewGameBanner,
                binding.imageViewGameBanner
            )
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
            val game = items[position]
            holder.bind(game, this)
            holder.itemView.visibility = View.VISIBLE
        } catch (e: IndexOutOfBoundsException) {
            Timber.e(
                e,
                "Could not display GameInfo. Invalid index $position on newItems with size $itemCount."
            )
            holder.itemView.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size

    /**
     * Presents a Filter for the [Filterable] interface.
     * Processes a search query when the Search function in the NavigationBar is used.
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterString = constraint?.toString()?.toLowerCase(Locale.ENGLISH)

                val results = FilterResults()

                if (filterString.isNullOrBlank()) {
                    results.values = items
                    results.count = items.size
                } else {
                    oldItems.filter { game ->
                        game.getName().toLowerCase(Locale.ENGLISH).contains(filterString)
                    }.let { filteredList ->
                        results.values = filteredList
                        results.count = filteredList.size
                    }
                }

                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val games = mutableListOf<Game>()
                val resultsValue = results?.values
                if (resultsValue is List<*>) {
                    for (item in resultsValue) {
                        if (item is Game) {
                            games.add(item)
                        }
                    }
                }

                showNewItems(games)
            }
        }
    }

    /**
     * Method from the [ColorListener] interface.
     *
     * When a CardView in the Library ListView has finished generating a background color for the
     * Card, we update the game so we can use the color in the transition to the GameActivity
     * when an item is clicked.
     */
    override fun onPrimaryGameColorCreated(game: Game, rgb: Int) {
        items.filter { item -> item.getAppId() == game.getAppId() }
            .forEach { item -> item.setPrimaryColor(rgb) }
    }

    /**
     * Set the games list shown in the RecyclerView attached to this adapter.
     * Sorts the list with the current sorting method before submitting.
     */
    fun updateGames(games: List<Game>) {
        items = games
        filter.filter(query)
    }

    /**
     * Update the search query. Displayed games must have the query String in their name.
     */
    fun updateQuery(query: String?) {
        this.query = query
        filter.filter(query)
    }

    private fun showNewItems(newItems: List<Game>) {
        this.items = newItems.sort(sortMethod)

        val diffResult = DiffUtil.calculateDiff(
            GamesDiffCallback(oldItems, this.items)
        )

        oldItems = newItems
        diffResult.dispatchUpdatesTo(this@GamesAdapter)
    }

    interface GamesAdapterCallback {
        fun onGameClicked(game: Game, imageView: ImageView, background: View, title: View)
    }
}