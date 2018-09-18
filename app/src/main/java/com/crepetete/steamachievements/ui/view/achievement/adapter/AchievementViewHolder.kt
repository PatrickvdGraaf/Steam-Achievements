package com.crepetete.steamachievements.ui.view.achievement.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.ui.activity.pager.TransparentPagerActivity
import javax.annotation.Nonnull


class AchievementViewHolder(private val view: View,
                            private val names: ArrayList<String?>) : RecyclerView.ViewHolder(view) {
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
                //                val alert = AchievementDialog(context, achievement)
//                alert.show()

                showAchievementPager(context)
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

    private fun showAchievementPager(@Nonnull context: Context) {
        val intent = Intent(context, TransparentPagerActivity::class.java)
        intent.putExtra(TransparentPagerActivity.INTENT_KEY_INDEX, startingIndex)
        intent.putExtra(TransparentPagerActivity.INTENT_KEY_NAME, names)

        context.startActivity(intent)
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