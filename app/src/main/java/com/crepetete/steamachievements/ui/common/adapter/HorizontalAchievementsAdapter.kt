package com.crepetete.steamachievements.ui.common.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.ui.activity.achievements.pager.TransparentPagerActivity
import com.crepetete.steamachievements.ui.common.adapter.viewholder.AchievementViewHolder
import com.crepetete.steamachievements.ui.common.enums.AchievSortingMethod
import com.crepetete.steamachievements.util.extensions.sortByLastAchieved
import com.crepetete.steamachievements.util.extensions.sortByNotAchieved
import com.crepetete.steamachievements.util.extensions.sortByRarity
import com.crepetete.steamachievements.vo.Achievement

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