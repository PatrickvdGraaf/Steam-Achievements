package com.crepetete.steamachievements.util.extensions

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import androidx.cardview.widget.CardView
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.vo.Game
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Sorts a list of games using the [Default] dispatcher.
 *
 * @return A list ordered according to the passed method param,
 * or an empty list if the method was invoked on a null object.
 */
suspend fun List<Game>?.sort(method: SortingType): List<Game> {
    if (this == null) {
        return listOf()
    }

    return when (method) {
        SortingType.PLAYTIME -> this.sortByPlaytime()
        SortingType.COMPLETION -> this.sortByCompletion()
        SortingType.NAME -> this.sortByName()
    }
}

suspend fun List<Game>.sortByCompletion(): List<Game> {
    return withContext(Default) {
        sortedWith(Comparator { o1, o2 ->
            val o1Percentage = o1.getPercentageCompleted()
            val o2Percentage = o2.getPercentageCompleted()
            when {
                o1Percentage == o2Percentage -> 0
                o1Percentage > o2Percentage -> -1
                else -> 1
            }
        })
    }
}

suspend fun List<Game>.sortByName(): List<Game> {
    return withContext(Default) {
        sortedWith(Comparator { o1, o2 ->
            val gameName = o1.getName()
            gameName.compareTo(o2.getName())
        })
    }
}

suspend fun List<Game>.sortByPlaytime(): List<Game> {
    return withContext(Default) {
        sortedWith(Comparator { o1, o2 ->
            when {
                o1.getPlaytime() == o2.getPlaytime() -> 0
                o1.getPlaytime() > o2.getPlaytime() -> -1
                else -> 1
            }
        })
    }
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