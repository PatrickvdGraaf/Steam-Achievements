package com.crepetete.steamachievements.util.extensions

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.cardview.widget.CardView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.vo.GameWithAchievements
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Sorts a list of games.
 *
 * @return A list ordered according to the passed method param,
 * or an empty list if the method was invoked on a null object.
 */
//fun List<Game>?.sort(method: SortingType): List<Game> {
//    if (this == null) {
//        return listOf()
//    }
//
//    return when (method) {
//        SortingType.PLAYTIME -> this.sortByPlaytime()
//        SortingType.COMPLETION -> this.sortByCompletion()
//        SortingType.NAME -> this.sortByName()
//    }
//}

fun List<GameWithAchievements>?.sort(method: SortingType): List<GameWithAchievements> {
    if (this == null) {
        return listOf()
    }

    return when (method) {
        SortingType.PLAYTIME -> this.sortByPlaytime()
        SortingType.COMPLETION -> this.sortByCompletion()
        SortingType.NAME -> this.sortByName()
    }
}

fun List<GameWithAchievements>.sortByCompletion(): List<GameWithAchievements> {
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

fun List<GameWithAchievements>.sortByName(): List<GameWithAchievements> {
    return sortedWith(Comparator { o1, o2 ->
        val gameName = o1.getName()
        gameName.compareTo(o2.getName())
    })
}

fun List<GameWithAchievements>.sortByPlaytime(): List<GameWithAchievements> {
    return sortedWith(Comparator { o1, o2 ->
        when {
            o1.getPlaytime() == o2.getPlaytime() -> 0
            o1.getPlaytime() > o2.getPlaytime() -> -1
            else -> 1
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

fun View.setBackgroundColorAnimated(colorFrom: Int, colorTo: Int?, duration: Long = 200) {
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

fun CardView.setBackgroundColorAnimated(colorFrom: Int, colorTo: Int?, duration: Long = 400) {
    if (colorTo == null) {
        return
    }

    val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
    colorAnimation.duration = duration // milliseconds
    colorAnimation.addUpdateListener { animator ->
        setCardBackgroundColor(animator.animatedValue as Int)
    }
    colorAnimation.start()
}

fun TextView.setColor(color: Int) {
    setTextColor(color)
    compoundDrawables.forEach { drawable: Drawable? ->
        drawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}

fun Date.getDaysFromNow(): Long {
    return TimeUnit.DAYS.convert(Calendar.getInstance().time.time - time, TimeUnit.MILLISECONDS)
}

fun <T : View> Activity.bind(@IdRes idRes: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return unsafeLazy { findViewById<T>(idRes) }
}

fun <T : View> View.bind(@IdRes idRes: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return unsafeLazy { findViewById<T>(idRes) }
}

fun <T : View> Dialog.bind(@IdRes idRes: Int): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return unsafeLazy { findViewById<T>(idRes) }
}

/**
 * Appends all elements that are not `null` to the given [destination].
 */
public fun <C : MutableCollection<in T>, T : Any> Iterable<T?>.filterNotNullTo(destination: C): C {
    for (element in this) if (element != null) destination.add(element)
    return destination
}

private fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)