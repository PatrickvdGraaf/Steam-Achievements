package com.crepetete.steamachievements.ui.common.adapter.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.decode.DataSource
import coil.request.Request
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.ui.common.loader.PulsatorLayout
import com.crepetete.steamachievements.vo.Achievement
import timber.log.Timber

class AchievementViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    private val textViewTitle = view.findViewById<TextView>(R.id.textViewTitle)
    private val pulsator = view.findViewById<PulsatorLayout>(R.id.pulsator)
    val imageView: ImageView = view.findViewById(R.id.imageView)

    private var startingIndex = 0

    fun bind(achievement: Achievement, index: Int) {
        this.startingIndex = index
        textViewTitle.text = achievement.displayName

        val context = view.context
        if (context != null) {

            pulsator.visibility = View.VISIBLE
            pulsator.start()

            imageView.load(
                if (achievement.achieved) {
                    achievement.iconUrl
                } else {
                    achievement.iconGrayUrl
                }
            ) {
                listener(object : Request.Listener {
                    override fun onError(data: Any, throwable: Throwable) {
                        super.onError(data, throwable)
                        Timber.w(
                            throwable,
                            "Error while loading image from url: ${achievement.iconUrl}."
                        )
                        pulsator.stop()
                    }

                    override fun onCancel(data: Any) {
                        super.onCancel(data)
                        pulsator.stop()
                    }

                    override fun onStart(data: Any) {
                        super.onStart(data)
                        pulsator.visibility = View.VISIBLE
                        pulsator.start()
                    }

                    override fun onSuccess(data: Any, source: DataSource) {
                        super.onSuccess(data, source)
                        pulsator.stop()
                        pulsator.visibility = View.GONE
                    }
                })
            }
        }
    }
}