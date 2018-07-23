package com.crepetete.steamachievements.ui.view.achievement.adapter

import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.model.Achievement


class AchievementViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private lateinit var achievement: Achievement

    private val imageViewIcon = view.findViewById<ImageButton>(R.id.icon)
    private val textViewUnlock = view.findViewById<TextView>(R.id.textView_unlock)

    fun bind(context: Context, achievement: Achievement) {
        this.achievement = achievement
        textViewUnlock.text = achievement.getDateString()

        imageViewIcon.setOnClickListener {
            AlertDialog.Builder(context)
                    .setTitle(achievement.displayName)
                    .setMessage(achievement.description)
                    .show()
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