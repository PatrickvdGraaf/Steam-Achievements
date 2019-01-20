package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.vo.Achievement

class AchievementViewHolder(private val view: View)
    : RecyclerView.ViewHolder(view) {
    private lateinit var achievement: Achievement

    private val imageViewIcon = view.findViewById<ImageButton>(R.id.imageViewIcon)
    private val textViewTitle = view.findViewById<TextView>(R.id.textViewTitle)

    private var startingIndex = 0

    fun bind(achievement: Achievement, index: Int) {
        this.achievement = achievement
        this.startingIndex = index
        textViewTitle.text = achievement.displayName

        val context = view.context
        if (context != null) {
            Glide.with(context)
                .load(if (achievement.achieved) {
                    achievement.iconUrl
                } else {
                    achievement.iconGrayUrl
                })
                .into(imageViewIcon)
        }
    }

    private fun getDescription(achievement: Achievement): String {
        return if (achievement.description != null) {
            "Global achievement rate: ${achievement.percentage}%\n\n" +
                "${achievement.description}"
        } else {
            "\"Global achievement rate: ${achievement.percentage}%"
        }
    }
}