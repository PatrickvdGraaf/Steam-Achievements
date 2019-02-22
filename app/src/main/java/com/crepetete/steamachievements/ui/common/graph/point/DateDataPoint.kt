package com.crepetete.steamachievements.ui.common.graph.point

import com.jjoe64.graphview.series.DataPoint
import java.util.*

/**
 * Extension to a [DataPoint] initialized with a Date.
 * Saves the [Date] value, as tbe super class will convert it to a y-value ([Double]).
 */
class DateDataPoint(val date: Date, y: Double) : DataPoint(date, y)