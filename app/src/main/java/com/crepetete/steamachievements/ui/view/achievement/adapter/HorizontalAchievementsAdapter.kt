package com.crepetete.steamachievements.ui.view.achievement.adapter

import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.ui.activity.pager.TransparentPagerActivity
import com.crepetete.steamachievements.utils.sortByLastAchieved
import com.crepetete.steamachievements.utils.sortByNotAchieved
import com.crepetete.steamachievements.utils.sortByRarity

class HorizontalAchievementsAdapter(
        private var sortingMethod: AchievSortingMethod = AchievSortingMethod.ACHIEVED)
    : RecyclerView.Adapter<AchievementViewHolder>() {

    private val achievements = mutableListOf<Achievement>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.list_achievement, parent, false)
        return AchievementViewHolder(view) { index ->
            val context = parent.context
            if (context != null) {
                val intent = Intent(context, TransparentPagerActivity::class.java)
                intent.putExtra(TransparentPagerActivity.INTENT_KEY_INDEX, index)
                intent.putExtra(TransparentPagerActivity.INTENT_KEY_APP_ID, ArrayList(achievements.map {
                    it.appId
                }))
                intent.putExtra(TransparentPagerActivity.INTENT_KEY_NAME, ArrayList(achievements.map {
                    it.name
                }))

                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = achievements.size

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position], position)
    }

    fun setAchievements(achievements: List<Achievement>) {
        this.achievements.clear()
        this.achievements.addAll(achievements)
        notifyDataSetChanged()
        // TODO Diff
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