package com.crepetete.steamachievements.ui.common.adapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.databinding.ItemGameBinding
import com.crepetete.steamachievements.vo.GameData
import com.crepetete.steamachievements.vo.GameWithAchievements

/**
 * Created at 19 January, 2019.
 */
class GameViewHolder(private val binding: ItemGameBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(game: GameWithAchievements?) {
        if (game != null) {
            val dataItem = GameData(game)
            binding.gameData = dataItem

            binding.background.setBackgroundColor(game.getPrimaryColor())
        }
    }
}