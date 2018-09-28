package com.crepetete.steamachievements.ui.view.achievement.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.model.Achievement

class AchievementViewHolder(private val view: View, private val funShowPager: (Int) -> Unit)
    : RecyclerView.ViewHolder(view) {
    private lateinit var achievement: Achievement

    private val imageViewIcon = view.findViewById<ImageButton>(R.id.icon)
    private val textViewTitle = view.findViewById<TextView>(R.id.textView_title)

    private var startingIndex = 0

    fun bind(achievement: Achievement, index: Int) {
        this.achievement = achievement
        this.startingIndex = index
        textViewTitle.text = achievement.displayName

        val context = view.context
        if (context != null) {
            imageViewIcon.setOnClickListener {
                showAchievementPager()
            }

            Glide.with(context)
                    .load(if (achievement.achieved) {
                        achievement.iconUrl
                    } else {
                        achievement.iconGrayUrl
                    })
                    .into(imageViewIcon)
        }
    }

    private fun showAchievementPager() {
        funShowPager(startingIndex)
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