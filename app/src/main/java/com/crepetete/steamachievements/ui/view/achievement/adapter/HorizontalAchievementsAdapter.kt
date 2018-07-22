package com.crepetete.steamachievements.ui.view.achievement.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.utils.sortByLastAchieved
import com.crepetete.steamachievements.utils.sortByNotAchieved

class HorizontalAchievementsAdapter(private val baseView: BaseView,
                                    private var sortingMethod: AchievSortingMethod
                                    = AchievSortingMethod.ACHIEVED)
    : RecyclerView.Adapter<AchievementViewHolder>() {

    private val achievements = mutableListOf<Achievement>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val inflater = LayoutInflater.from(baseView.getContext())
        val view = inflater.inflate(R.layout.list_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun getItemCount(): Int {
        return achievements.size
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(baseView.getContext(), achievements[position])
    }

    fun setAchievements(achievements: List<Achievement>) {
        this.achievements.clear()
        this.achievements.addAll(achievements)
        notifyDataSetChanged()
        // TODO Diff
    }

    fun updateSortingMethod(specificMethod: AchievSortingMethod? = null): String {
        sortingMethod = specificMethod ?: when (sortingMethod) {
            AchievSortingMethod.ACHIEVED -> AchievSortingMethod.NOT_ACHIEVED
            AchievSortingMethod.NOT_ACHIEVED -> AchievSortingMethod.ACHIEVED
        }
        sort()
        return sortingMethod.description
    }

    private fun sort() {
        when (sortingMethod) {
            AchievSortingMethod.ACHIEVED -> { setAchievements(achievements.sortByLastAchieved()) }
            AchievSortingMethod.NOT_ACHIEVED -> { setAchievements(achievements.sortByNotAchieved()) }
        }
    }
}