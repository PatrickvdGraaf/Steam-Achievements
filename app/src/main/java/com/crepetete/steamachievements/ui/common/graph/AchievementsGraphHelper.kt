package com.crepetete.steamachievements.ui.common.graph

import android.graphics.DashPathEffect
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.vo.Achievement
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.util.*

/**
 * A Class for setting info on a [GraphView].
 */
class AchievementsGraphHelper {
    private val steamReleaseDate = Calendar.getInstance().apply {
        set(2003, 9, 12, 0, 0, 0)
    }.time

    /**
     * Shows a Graph where a line indicates the total completion percentage for a game over time.
     */
    fun setAchievementsOverTime(graphView: GraphView, achievements: List<Achievement>) {
        val context = graphView.context
        if (context != null) {
            //            val sortedAchievements = achievements.sortByLastAchieved()
            //                    .filter { it.unlockTime != null && it.unlockTime != Date() && it.unlockTime!!.after(steamReleaseDate) }

            val pairs = achievements
                    .map { achievement ->
                        val achievementsBeforeAchievement = achievements
                                .filter(Achievement::achieved)
                                .filter {
                                    it.unlockTime!!.before(achievement.unlockTime)
                                }

                        val completionPercentage = (achievementsBeforeAchievement.size.toDouble()
                                / achievements.size.toDouble())

                        // Declaring nonnull because we checked earlier.
                        Pair(achievement.unlockTime!!, completionPercentage * 100)
                    }

            val dataPoints = pairs.sortedBy { it.first }.map { DataPoint(it.first, it.second) }

            // You can directly pass Date objects to DataPoint-Constructor
            // This will convert the Date to double via Date#getTime()
            val series = LineGraphSeries(dataPoints.toTypedArray())

            graphView.addSeries(series)

            // Set date label formatter
            if (pairs.isNotEmpty()) {
                // Set manual x bounds to have nice steps in the graph
                graphView.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(context)
                graphView.gridLabelRenderer.numHorizontalLabels = 3
                graphView.gridLabelRenderer.horizontalLabelsColor = ContextCompat.getColor(context,
                        R.color.colorTextLabel)
                graphView.gridLabelRenderer.verticalLabelsColor = ContextCompat.getColor(context,
                        R.color.colorTextLabel)
                graphView.gridLabelRenderer.gridColor = ContextCompat.getColor(context,
                        R.color.colorPrimary)

                // Styling series
                val paint = Paint()
                paint.style = Paint.Style.STROKE
                paint.color = ContextCompat.getColor(context, R.color.colorAccent)
                paint.strokeWidth = 4F
                paint.pathEffect = DashPathEffect(floatArrayOf(8f, 5f), 0f)
                series.setCustomPaint(paint)

                // Setting the result of minBy to nonnull, because we checked if the pairs array
                // was not empty.
                graphView.viewport.setMinX(pairs.minBy {
                    it.first
                }!!.first.time.toDouble())

                graphView.viewport.setMaxX(pairs.maxBy {
                    it.first
                }!!.first.time.toDouble())

                // Set manual Y bounds
                graphView.viewport.isYAxisBoundsManual = true
                graphView.viewport.setMinY(0.0)
                graphView.viewport.setMaxY(100.0)
            }

            // As we use dates as labels, the human rounding to nice readable numbers
            // is not necessary.
            graphView.gridLabelRenderer.setHumanRounding(false)
        }
    }
}