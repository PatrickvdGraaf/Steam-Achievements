package com.crepetete.steamachievements.utils

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.support.annotation.Size
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import io.reactivex.annotations.CheckReturnValue
import io.reactivex.annotations.SchedulerSupport
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.internal.functions.ObjectHelper
import io.reactivex.internal.observers.ConsumerSingleObserver
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit


fun List<Game>.sortByCompletion(): List<Game> {
    return sortedWith(Comparator { o1, o2 ->
        val o1Percentage = o1.getPercentageCompleted()
        val o2Percentage = o2.getPercentageCompleted()
        when {
            o1Percentage == o2Percentage -> 0
            o1Percentage > o2Percentage -> -1
            else -> 1
        }
    })
}

fun List<Game>.sortByName(): List<Game> {
    return sortedWith(Comparator { o1, o2 ->
        o1.name.compareTo(o2.name)
    })
}

fun List<Game>.sortByPlaytime(): List<Game> {
    return sortedWith(Comparator { o1, o2 ->
        when {
            o1.playTime == o2.playTime -> 0
            o1.playTime > o2.playTime -> -1
            else -> 1
        }
    })
}

fun List<Achievement>.sortByLastAchieved(): List<Achievement> {
    return sortedWith(kotlin.Comparator { o1, o2 ->
        try {
            if (o1.unlockTime != null && o2.unlockTime != null) {
                when {
                    o1.unlockTime == o2.unlockTime -> 0
                    o1.unlockTime!!.after(o2.unlockTime) -> -1
                    else -> 1
                }
            } else if (o1.unlockTime == null && o2.unlockTime != null) {
                1
            } else if (o1.unlockTime == null && o2.unlockTime == null) {
                0
            } else {
                -1
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }

    })
}

fun List<Achievement>.sortByNotAchieved(): List<Achievement> {
    return sortedWith(kotlin.Comparator { o1, o2 ->
        when {
            o1.achieved == o2.achieved -> 0
            o1.achieved && !o2.achieved -> 1
            else -> -1
        }
    })
}

fun List<Achievement>.sortByRarity(): List<Achievement> {
    return sortedWith(kotlin.Comparator { o1, o2 ->
        when {
            o1.percentage > o2.percentage -> 1
            o1.percentage < o2.percentage -> -1
            else -> 0
        }
    })
}

fun Long.toHours(context: Context? = null): String {
    val hours = this / 60
    val minutes = this % 60
    var hoursAbbr = "h"
    var minAbbr = "m"
    if (context != null) {
        hoursAbbr = context.getString(R.string.abbr_hours)
        minAbbr = context.getString(R.string.abbr_minutes)
    }

    return if (hours <= 0) {
        "$minutes$minAbbr"
    } else {
        "$hours$hoursAbbr, $minutes$minAbbr"
    }
}

fun Int.toPercentage(from: Int): Long {
    if (from == 0) {
        return 0L
    }
    return this * 100L / from
}

fun Context.isConnectedToInternet(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

fun View.setBackgroundColorAnimated(colorFrom: Int, colorTo: Int?, duration: Long = 300) {
    if (colorTo == null) {
        return
    }

    val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
    colorAnimation.duration = duration // milliseconds
    colorAnimation.addUpdateListener { animator ->
        setBackgroundColor(animator.animatedValue as Int)
    }
    colorAnimation.start()
}

fun TextView.setColor(color: Int) {
    setTextColor(color)
    compoundDrawables.forEach { drawable: Drawable? ->
        drawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

fun TextView.setCompletedFlag(isCompleted: Boolean) {
    compoundDrawablePadding = 16
    setCompoundDrawablesWithIntrinsicBounds(
            if (isCompleted) {
                R.drawable.ic_completed_24dp
            } else {
                0
            }, 0, 0, 0)
}

fun ProgressBar.animateToPercentage(@Size(max = 100) percentage: Int, duration: Long = 300) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        setProgress(percentage, true)
    } else {
        progress = percentage
        val animation = ObjectAnimator.ofInt(this, "progress",
                this.progress, percentage)
        animation.duration = duration
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }
}

fun Date.getDaysFromNow(): Long {
    return TimeUnit.DAYS.convert(Calendar.getInstance().time.time - time, TimeUnit.MILLISECONDS)
}