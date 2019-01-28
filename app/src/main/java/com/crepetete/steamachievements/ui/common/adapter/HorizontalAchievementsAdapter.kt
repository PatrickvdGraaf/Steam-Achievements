package com.crepetete.steamachievements.ui.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.ui.common.adapter.sorting.AchievementSortedListImpl
import com.crepetete.steamachievements.ui.common.adapter.viewholder.AchievementViewHolder
import com.crepetete.steamachievements.vo.Achievement

/**
 * Adapter for a horizontal list of [Achievement]s displayed in a [AchievementViewHolder].
 * Can sort using a [Comparator]<[Achievement]>.
 */
class HorizontalAchievementsAdapter(private val listener: OnAchievementClickListener) : RecyclerView.Adapter<AchievementViewHolder>() {

    private val sortedListComparatorWrapper = AchievementSortedListImpl(this)

    private var achievements = SortedList(Achievement::class.java,
        SortedList.BatchedCallback<Achievement>(sortedListComparatorWrapper))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_achievement, parent, false)
        val viewHolder = AchievementViewHolder(view)

        view.findViewById<View>(R.id.imageViewIcon).setOnClickListener {
            listener.onAchievementClick(viewHolder.adapterPosition)
        }
        return viewHolder
    }

    override fun getItemCount() = achievements.size()

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position], position)
    }

    fun setAchievements(achievements: List<Achievement>) {
        with(this.achievements) {
            beginBatchedUpdates()
            addAll(achievements)
            endBatchedUpdates()
        }
    }

    /**
     * Sets a new sorting method for the Achievements if it differs from the current method.
     */
    fun updateSortingMethod(value: Comparator<Achievement>) {
        with(achievements) {
            sortedListComparatorWrapper.setComparator(value)
            beginBatchedUpdates()
            val tempUsers = (0 until achievements.size()).mapTo(ArrayList<Achievement>()) { get(it) }
            clear()
            addAll(tempUsers)
            tempUsers.clear()
            endBatchedUpdates()
        }
    }

    interface OnAchievementClickListener {
        fun onAchievementClick(index: Int)
    }
}