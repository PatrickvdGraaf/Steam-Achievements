package com.crepetete.steamachievements.util.extensions

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.presentation.common.enums.SortingType
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers.Default
import java.util.Comparator


/**
 * Sorts a list of games using the [Default] dispatcher.
 *
 * @return A list ordered according to the passed method param,
 * or an empty list if the method was invoked on a null object.
 */
fun List<Game>?.sort(method: SortingType): List<Game> {
    if (this == null) {
        return listOf()
    }

    return when (method) {
        SortingType.PLAYTIME -> sortByPlaytime()
        SortingType.COMPLETION -> sortByCompletion()
        SortingType.NAME -> sortByName()
    }
}

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
        val gameName = o1.getName()
        gameName.compareTo(o2.getName())
    })
}

fun List<Game>.sortByPlaytime(): List<Game> {
    return sortedWith(Comparator { o1, o2 ->
        when {
            o1.getPlaytime() == o2.getPlaytime() -> 0
            o1.getPlaytime() > o2.getPlaytime() -> -1
            else -> 1
        }
    })
}

fun View.animateBackground(colorFrom: Int, colorTo: Int?, duration: Long = 200) {
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

fun CardView.animateBackground(
    @ColorInt colorFrom: Int,
    @ColorInt colorTo: Int?,
    duration: Long = 400
) {
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

fun LineDataSet.customizeDataSet(dataSetSize: Int, chart: LineChart): LineDataSet {
    this.setDrawFilled(true)
    this.setDrawValues(false)

    this.color = R.color.colorAccent

    this.fillDrawable = ContextCompat.getDrawable(
        chart.context,
        R.drawable.gradient_white_to_transparent
    )

    this.setColors(color)

    for (index in 0..dataSetSize - 2) {
        this.circleColors[0] = color
    }
    return this
}

fun <R> bindObserver(
    observer: MediatorLiveData<R?>?,
    source: LiveData<R?>
) {
    observer?.apply {
        addSource(source) {
            postValue(it)
        }
    }
}