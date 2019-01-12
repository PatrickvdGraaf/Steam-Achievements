package com.crepetete.steamachievements.binding

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.crepetete.steamachievements.R

/**
 * Data Binding adapters specific to the app.
 */
object BindingAdapters {
    @JvmStatic
    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    /**
     * Let's an image accept an url in xml which will be loaded in using [Glide].
     */
    @BindingAdapter("android:src")
    fun setImageUrl(view: ImageView, url: String) {
        Glide.with(view.context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(view)
    }

    /**
     * Shows a flag in front of a TextView.
     */
    @BindingAdapter("completionFlagVisibility")
    fun setFlagVisibility(view: TextView, visible: Boolean) {
        view.compoundDrawablePadding = 16
        view.setCompoundDrawablesWithIntrinsicBounds(
            if (visible) {
                R.drawable.ic_completed_24dp
            } else {
                0
            }, 0, 0, 0)
    }

    /**
     * Animates the progress from 0 to the given progress param.
     */
    @BindingAdapter("progressAnimated")
    fun setProgressAnimated(view: ProgressBar, progress: Float) {
        val animationDuration: Long = 1000
        view.progress = 0
        val percentage = progress.toInt()
        view.startAnimation(object : Animation() {
            var mTo = if (percentage < 0) 0 else if (percentage > view.max) view.max else percentage
            var mFrom = progress

            init {
                duration = (Math.abs(mTo - mFrom) * (animationDuration / view.max)).toLong()
            }

            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                val value = mFrom + (mTo - mFrom) * interpolatedTime
                view.progress = value.toInt()
            }
        })
    }
}