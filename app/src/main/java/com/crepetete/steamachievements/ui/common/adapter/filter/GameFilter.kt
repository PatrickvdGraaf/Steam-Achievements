package com.crepetete.steamachievements.ui.common.adapter.filter

import android.widget.Filter
import com.crepetete.steamachievements.vo.Game

/**
 * Created at 25 January, 2019.
 */
class GameFilter(
    private var games: List<Game>,
    private val listener: GameFilterListener
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val filterString = constraint?.toString()?.toLowerCase()

        val results = Filter.FilterResults()

        if (filterString.isNullOrBlank()) {
            results.values = games
            results.count = games.size
        } else {
            games.filter { game ->
                game.getName().toLowerCase().contains(filterString)
            }.let { filteredList ->
                results.values = filteredList
                results.count = filteredList.size
            }
        }

        return results
    }

    @Suppress("UNCHECKED_CAST")
    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        listener.updateFilteredData(results?.values as List<Game>)
    }

    fun updateGames(newGames: List<Game>) {
        games = newGames
    }
}