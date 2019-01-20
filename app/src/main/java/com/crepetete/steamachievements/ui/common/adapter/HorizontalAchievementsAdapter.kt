package com.crepetete.steamachievements.ui.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.ui.activity.achievements.pager.TransparentPagerActivity
import com.crepetete.steamachievements.ui.common.adapter.diffutil.AchievementDiffCallback
import com.crepetete.steamachievements.ui.common.adapter.viewholder.AchievementViewHolder
import com.crepetete.steamachievements.ui.common.enums.AchievSortingMethod
import com.crepetete.steamachievements.util.extensions.sortByLastAchieved
import com.crepetete.steamachievements.util.extensions.sortByNotAchieved
import com.crepetete.steamachievements.util.extensions.sortByRarity
import com.crepetete.steamachievements.vo.Achievement

class HorizontalAchievementsAdapter(
    private var sortingMethod: AchievSortingMethod = AchievSortingMethod.ACHIEVED
) : RecyclerView.Adapter<AchievementViewHolder>() {

    private var achievements = listOf<Achievement>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_achievement, parent, false)
        val viewHolder = AchievementViewHolder(view)

        view.findViewById<View>(R.id.imageViewIcon).setOnClickListener { v ->
            v.context.startActivity(TransparentPagerActivity.getInstance(parent.context, viewHolder.adapterPosition, achievements))
        }
        return viewHolder
    }

    override fun getItemCount() = achievements.size

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position], position)
    }

    fun setAchievements(achievements: List<Achievement>) {
        val diffResult = DiffUtil.calculateDiff(AchievementDiffCallback(achievements, this.achievements))
        diffResult.dispatchUpdatesTo(this)

        this.achievements = achievements
    }

    fun updateSortingMethod(specificMethod: AchievSortingMethod? = null): String {
        if (sortingMethod != specificMethod) {
            sortingMethod = specificMethod ?: when (sortingMethod) {
                AchievSortingMethod.ACHIEVED -> AchievSortingMethod.NOT_ACHIEVED
                AchievSortingMethod.NOT_ACHIEVED -> AchievSortingMethod.RARITY
                AchievSortingMethod.RARITY -> AchievSortingMethod.ACHIEVED
            }
            sort()
        }
        return sortingMethod.description
    }

    private fun sort() {
        when (sortingMethod) {
            AchievSortingMethod.ACHIEVED -> {
                setAchievements(achievements.sortByLastAchieved())
            }
            AchievSortingMethod.NOT_ACHIEVED -> {
                setAchievements(achievements.sortByNotAchieved())
            }
            AchievSortingMethod.RARITY -> {
                setAchievements(achievements.sortByRarity())
            }
        }
    }
}