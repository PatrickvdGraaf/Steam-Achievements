package com.crepetete.steamachievements.presentation.common.adapter.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.presentation.common.loader.PulsatorLayout

class AchievementViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    private val textViewTitle = view.findViewById<TextView>(R.id.textViewTitle)
    private val pulsator = view.findViewById<PulsatorLayout>(R.id.pulsator)

    val imageView: ImageView = view.findViewById(R.id.imageView)

    fun bind(achievement: Achievement) {
        textViewTitle.text = achievement.displayName

        val context = view.context
        if (context != null) {

            val imageUrl = if (achievement.achieved) {
                achievement.iconUrl
            } else {
                achievement.iconGrayUrl
            }

            pulsator

            Glide.with(context)
                .load(imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }
}