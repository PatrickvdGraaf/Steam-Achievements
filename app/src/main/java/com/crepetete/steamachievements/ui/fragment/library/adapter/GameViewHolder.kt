package com.crepetete.steamachievements.ui.fragment.library.adapter

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.utils.animateToPercentage
import com.crepetete.steamachievements.utils.setBackgroundColorAnimated
import com.crepetete.steamachievements.utils.setCompletedFlag

class GameViewHolder(private val baseView: BaseView, private val view: View,
                     private val listener: GamesAdapter.Listener? = null)
    : RecyclerView.ViewHolder(view) {
    private lateinit var game: Game

    private val container = view.findViewById<ConstraintLayout>(R.id.content)

    private val imageView = view.findViewById<ImageView>(R.id.game_banner)
    private val titleTextView = view.findViewById<TextView>(R.id.name_textView)
    private val recentPlayedTextView = view.findViewById<TextView>(
            R.id.recently_played_textView)
    private val totalPlayedTextView = view.findViewById<TextView>(
            R.id.total_played_textView)
    private val achievementsTextView = view.findViewById<TextView>(
            R.id.achievements_textView)
    private val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

    fun bind(game: Game, listener: GamesAdapter.Listener?) {
        this.game = game

        titleTextView.text = game.name
        totalPlayedTextView.text = game.getTotalPlayTimeString(baseView.getContext())

        Glide.with(baseView.getContext())
                .load(game.getFullLogoUrl())
                .into(object : SimpleTarget<Drawable>() {
                    /**
                     * The method that will be called when the resource load has finished.
                     *
                     * @param resource the loaded resource.
                     */
                    override fun onResourceReady(resource: Drawable,
                                                 transition: Transition<in Drawable>?) {
                        imageView.setImageDrawable(resource)
                        if (game.colorPrimaryDark == 0 && resource is BitmapDrawable) {
                            animateBackground(resource.bitmap)
                        } else {
                            container.setBackgroundColor(game.colorPrimaryDark)
                        }
                    }
                })

        listener?.let { l ->
            view.setOnClickListener { l.onGameSelected(game) }
        }

        if (game.recentPlayTime > 0) {
            recentPlayedTextView.text = game.getRecentPlaytimeString(baseView.getContext())
            recentPlayedTextView.visibility = View.VISIBLE
        } else {
            recentPlayedTextView.visibility = View.INVISIBLE
        }

        if (game.hasAchievements()) {
            progressBar.visibility = View.VISIBLE
            achievementsTextView.visibility = View.VISIBLE
            val percentage = game.getPercentageCompleted().toInt()
            if (percentage > 0) {
                if (progressBar.progress == 0) {
                    progressBar.animateToPercentage(percentage)
                }
            } else {
                progressBar.progress = percentage
            }
            achievementsTextView.text = game.getAchievementsText()
            achievementsTextView.setCompletedFlag(game.isCompleted())
        } else {
            progressBar.visibility = View.GONE
            achievementsTextView.visibility = View.GONE
        }
    }

    private fun animateBackground(bitmap: Bitmap) {
        Palette.from(bitmap).generate {
            val swatch = it.darkMutedSwatch

            if (swatch?.rgb != null) {
                container.setBackgroundColorAnimated(
                        ContextCompat.getColor(view.context,
                                R.color.colorGameViewHolderTitleBackground),
                        swatch.rgb)

                game.colorPrimaryDark = swatch.rgb
                listener?.updateGame(game)
            }
        }
    }
}