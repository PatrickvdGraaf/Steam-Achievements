package com.crepetete.steamachievements.presentation.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.presentation.common.adapter.sorting.AchievementSortedListImpl
import com.crepetete.steamachievements.presentation.common.adapter.viewholder.AchievementViewHolder

/**
 * Adapter for a horizontal list of [Achievement]s displayed in a [AchievementViewHolder].
 *
 * Can sort using a [Comparator]<[Achievement]>.
 */
class HorizontalAchievementsAdapter(
    private val listener: OnAchievementClickListener? = null,
    var smallLayout: Boolean = false
) : RecyclerView.Adapter<AchievementViewHolder>() {

    private val sortedListComparatorWrapper = AchievementSortedListImpl(this)

    private var achievements = SortedList(
        Achievement::class.java,
        SortedList.BatchedCallback<Achievement>(sortedListComparatorWrapper))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(if (smallLayout) {
            R.layout.view_holder_achievement_small
        } else {
            R.layout.view_holder_achievement
        }, parent, false)

        val viewHolder = AchievementViewHolder(view)

        viewHolder.imageView.setOnClickListener {
            listener?.onAchievementClick(viewHolder.adapterPosition, getAchievementsAsList())
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount() = achievements.size()

    /**
     * Initially sets the lists content.
     * Doesn't need to compare it to the current list.
     */
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
            val tempAchievements = (0 until achievements.size()).mapTo(ArrayList<Achievement>()) { get(it) }
            clear()
            addAll(tempAchievements)
            tempAchievements.clear()
            endBatchedUpdates()
        }
    }

    /**
     * Move content from the [SortedList] into a normal [List].
     */
    private fun getAchievementsAsList(): List<Achievement> = mutableListOf<Achievement>().apply {
        var i = 0
        while (i < achievements.size()) {
            add(i, achievements[i])
            i++
        }
    }

    interface OnAchievementClickListener {
        fun onAchievementClick(index: Int, sortedList: List<Achievement>)
    }
}