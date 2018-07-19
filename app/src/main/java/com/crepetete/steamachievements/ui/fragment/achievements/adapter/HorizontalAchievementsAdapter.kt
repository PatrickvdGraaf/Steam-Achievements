package com.crepetete.steamachievements.ui.fragment.achievements.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.model.Achievement

class HorizontalAchievementsAdapter(private val baseView: BaseView) : RecyclerView.Adapter<AchievementViewHolder>() {
    private val recentAchievements = mutableListOf<Achievement>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val inflater = LayoutInflater.from(baseView.getContext())
        val view = inflater.inflate(R.layout.list_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun getItemCount(): Int {
        return recentAchievements.size
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(baseView.getContext(), recentAchievements[position])
    }

    fun setRecentAchievements(achievements: List<Achievement>) {
        recentAchievements.clear()
        recentAchievements.addAll(achievements)
        notifyDataSetChanged()
    }
}