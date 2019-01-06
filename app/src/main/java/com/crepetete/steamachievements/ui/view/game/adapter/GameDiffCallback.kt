package com.crepetete.steamachievements.ui.view.game.adapter

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.crepetete.steamachievements.vo.Game

class GameDiffCallback(
        private val oldGamesList: List<Game>,
        private val newGamesList: List<Game>
) : DiffUtil.Callback() {
    /**
     * Called by the DiffUtil to decide whether two object represent the same Item.
     *
     *
     * For example, if your items have unique ids, this method should check their id equality.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list
     * @return True if the two items represent the same object or false if they are different.
     */
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldGamesList[oldItemPosition].appId == newGamesList[newItemPosition].appId
    }

    /**
     * Returns the size of the old list.
     *
     * @return The size of the old list.
     */
    override fun getOldListSize(): Int = oldGamesList.size

    /**
     * Returns the size of the new list.
     *
     * @return The size of the new list.
     */
    override fun getNewListSize(): Int = newGamesList.size

    /**
     * Called by the DiffUtil when it wants to check whether two items have the same data.
     * DiffUtil uses this information to detect if the contents of an item has changed.
     *
     *
     * DiffUtil uses this method to check equality instead of [Object.equals]
     * so that you can change its behavior depending on your UI.
     * For example, if you are using DiffUtil with a
     * [RecyclerView.Adapter][android.support.v7.widget.RecyclerView.Adapter], you should
     * return whether the items' visual representations are the same.
     *
     *
     * This method is called only if [.areItemsTheSame] returns
     * `true` for these items.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list which replaces the
     * oldItem
     * @return True if the contents of the items are the same or false if they are different.
     */
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldGame = oldGamesList[oldItemPosition]
        val newGame = newGamesList[newItemPosition]
        return oldGame.name == newGame.name
                && oldGame.playTime == newGame.playTime
                && oldGame.recentPlayTime == newGame.recentPlayTime
                && oldGame.iconUrl == newGame.iconUrl
                && oldGame.logoUrl == newGame.logoUrl
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //you can return particular field for changed item.
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}