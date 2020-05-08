package com.crepetete.steamachievements.binding

import androidx.databinding.BindingAdapter
import com.crepetete.steamachievements.ui.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.util.extensions.customizeDataSet
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.GameData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.Date

@BindingAdapter("chartData")
fun setChartData(chart: LineChart, gameData: GameData?) {
    val achievedEntries = ArrayList<Entry>()

    if (gameData != null) {
        val achievements = gameData.getAchievements()
        achievements
            .filter {
                it.achieved &&
                        it.unlockTime != Date() &&
                        it.unlockTime?.after(AchievementsGraphViewUtil.steamReleaseDate) == true
            }
            .sortedBy { it.unlockTime }
            .map { achievement -> achievedEntries.addEntry(achievements, achievement) }
    }

    val dataSets: MutableList<ILineDataSet> = ArrayList()

    val achievementsDataSet = LineDataSet(achievedEntries, "Completion")
        .customizeDataSet(achievedEntries.size, chart)

    achievementsDataSet.setDrawHighlightIndicators(false)

    dataSets.add(achievementsDataSet)

    val lineData = LineData(dataSets)
    chart.data = lineData
    chart.postInvalidate()
}

/**
 * This method creates an [Entry] for the [LineChart] showing Achievements completion percentages.
 *
 * It uses the size of the complete [achievements] list and the size of a filtered list containing
 * only unlocked Achievements to calculate the total completion percentage at the moment the user
 * unlocked the specific [achievement]. This value goes on the y-axis.
 *
 * The x-axis will contain the [Achievement.unlockTime] in millis.
 */
private fun ArrayList<Entry>.addEntry(
    achievements: List<Achievement>,
    achievement: Achievement
) {
    // Check the users completion rate after unlocking the [achievement].
    val unlockedAchievements = achievements
        .filter(Achievement::achieved)
        .map { it.unlockTime }
        .filter { it?.before(achievement.unlockTime) == true || it == achievement.unlockTime }

    // Calculate the percentage relative to the already achieved achievements at that time.
    val completionPercentage =
        (unlockedAchievements.size.toFloat() / achievements.size.toFloat())

    achievement.unlockTime?.time?.let {
        this.add(Entry(it.toFloat(), completionPercentage * 100F))
    }
}
