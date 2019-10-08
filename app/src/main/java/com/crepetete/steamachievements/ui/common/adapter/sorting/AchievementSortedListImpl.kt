package com.crepetete.steamachievements.ui.common.adapter.sorting

import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.vo.Achievement

/**
 * Created at 26 January, 2019.
 */
class AchievementSortedListImpl(
    adapter: RecyclerView.Adapter<*>
) : SortedListComparatorWrapper<Achievement>(adapter, DEFAULT_ORDER) {

    companion object {
        /* By default, set the sort by rarity, starting from lowest.
         * This is most likely what the player wants to know (in current use case, used in GameActivity. */
        val DEFAULT_ORDER = Order.LatestAchievedOrder()
    }

    override fun areContentsTheSame(oldItem: Achievement,
                                    newItem: Achievement) = oldItem == newItem

    override fun areItemsTheSame(item1: Achievement,
                                 item2: Achievement) = item1.name == item2.name
        && item1.appId == item2.appId
}