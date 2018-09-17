package com.crepetete.steamachievements.ui.view.achievement.adapter

import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.model.Achievement


class AchievementViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private lateinit var achievement: Achievement

    private val imageViewIcon = view.findViewById<ImageButton>(R.id.icon)
    private val textViewTitle = view.findViewById<TextView>(R.id.textView_title)

    fun bind(achievement: Achievement) {
        this.achievement = achievement
        textViewTitle.text = achievement.displayName

        val context = view.context
        if (context != null) {
            imageViewIcon.setOnClickListener {
                AlertDialog.Builder(context)
                        .setTitle(achievement.displayName)
                        .setMessage(getDescription(achievement))
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

    private fun getDescription(achievement: Achievement): String {
        return if (achievement.description != null) {
            "Global achievement rate: ${achievement.percentage}%\n\n" +
                    "${achievement.description}"
        } else {
            "\"Global achievement rate: ${achievement.percentage}%"
        }
    }
}