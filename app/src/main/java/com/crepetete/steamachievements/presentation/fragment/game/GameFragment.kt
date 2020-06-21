package com.crepetete.steamachievements.presentation.fragment.game

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.presentation.activity.achievements.TransparentPagerActivity
import com.crepetete.steamachievements.presentation.activity.news.NewsDetailActivity
import com.crepetete.steamachievements.presentation.common.adapter.HorizontalAchievementsAdapter
import com.crepetete.steamachievements.presentation.common.adapter.NewsAdapter
import com.crepetete.steamachievements.presentation.common.adapter.callback.OnNewsItemClickListener
import com.crepetete.steamachievements.presentation.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.presentation.common.graph.point.OnGraphDateTappedListener
import com.crepetete.steamachievements.presentation.fragment.BaseFragment
import com.crepetete.steamachievements.util.extensions.customizeDataSet
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.android.synthetic.main.fragment_game.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

/**
 * Shows a more detailed overview of the available information of a [Game] and its [Achievement]s.
 */
class GameFragment : BaseFragment(R.layout.fragment_game), OnGraphDateTappedListener,
    HorizontalAchievementsAdapter.OnAchievementClickListener {

    companion object {
        private const val INTENT_GAME = "INTENT_GAME"
        private const val TAG = "GAME_DETAIL_FRAGMENT"

        fun getInstance(game: Game): GameFragment {
            val bundle = Bundle()
            bundle.putParcelable(INTENT_GAME, game)

            val fragment = GameFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private val appId = arguments?.getParcelable<Game>(INTENT_GAME)?.getAppId()

    private val viewModel: GameViewModel by viewModel("VIEWMODEL_GAME_${appId}")

    private val newsAdapter by lazy {
        NewsAdapter(object : OnNewsItemClickListener {
            override fun onNewsItemSelected(item: NewsItem) {
                context?.let {
                    startActivity(NewsDetailActivity.getIntent(it, item))
                }
            }
        })
    }

    private val achievementsAdapter by lazy { HorizontalAchievementsAdapter(this) }

    override fun getFragmentName() = TAG

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setViewModelObservers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve data.
        arguments?.getParcelable<Game>(INTENT_GAME)?.let { game ->
            viewModel.setGame(game)
        }

        // Set Button Listeners.
        sortAchievementsButton.setOnClickListener {
            viewModel.setAchievementSortingMethod()
        }

        // Prepare RecyclerView.
        recyclerViewAchievements.layoutManager = LinearLayoutManager(
            view.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewNews.layoutManager = LinearLayoutManager(
            view.context,
            LinearLayoutManager.VERTICAL,
            false
        )

        // Set RecyclerView adapter.
        recyclerViewAchievements.adapter = achievementsAdapter
        recyclerViewAchievements.setHasFixedSize(true)
        recyclerViewNews.adapter = newsAdapter

        // Achievements Graph
        customizeChart()
    }

    private fun setViewModelObservers() {
        // Set observers
        viewModel.game.observe(viewLifecycleOwner, Observer { game ->
            if (game != null) {
                setGameInfo(game)
            }
        })

        viewModel.achievements.observe(viewLifecycleOwner, Observer { achievements ->
            setAchievementsInfo(achievements)
        })

        /* Update the achievement adapter sorting method.*/
        viewModel.getAchievementSortingMethod().observe(viewLifecycleOwner, Observer { method ->
            /* Update label. */
            sortMethodDescription.text = String.format("Sorted by: %s", method.getName(resources))

            /* Sort achievements in adapter. */
            achievementsAdapter.updateSortingMethod(method)
        })

        viewModel.news.observe(viewLifecycleOwner, Observer { nullableNews ->
            nullableNews?.let { news ->
                if (news.isNotEmpty()) {
                    newsAdapter.setItems(news)
                    showView(card_view_news)
                }
            }
        })

        viewModel.newsLoadingState.observe(viewLifecycleOwner, Observer {
            Timber.d("New Loading state: $it.")
        })

        viewModel.newsLoadingError.observe(viewLifecycleOwner, Observer {
            Timber.e("Loading News Failed: ${it?.localizedMessage}")
        })
    }

    private fun showView(contentView: View?) {
        contentView?.apply {
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            if (alpha == 0F) {
                animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setStartDelay(400)
                    .setListener(null)
            }
        }
    }

    override fun onAchievementClick(index: Int, sortedList: List<Achievement>) {
        context?.let {
            startActivity(TransparentPagerActivity.getInstance(it, index, sortedList))
        }
    }

    private fun setGameInfo(game: BaseGameInfo?) {
        if (game == null) {
            return
        }

        if (game.playTime > 0) {
            totalPlayedTextView.setText(toHours(game.playTime))

            if (game.recentPlayTime ?: 0 > 0) {
                textViewRecentlyPlayed.setText(toHours(game.recentPlayTime ?: 0))
                textViewRecentlyPlayed.visibility = View.VISIBLE
            } else {
                textViewRecentlyPlayed.visibility = View.GONE
            }

            showView(playTimeCardView)
        } else {
            playTimeCardView.visibility = View.GONE
        }
    }

    private fun setAchievementsInfo(achievements: List<Achievement>) {
        if (achievements.isNotEmpty()) {
            // Move achievements to adapter.
            achievementsAdapter.setAchievements(achievements)

            // Init Graph.
            setChartData(lineChartAchievements, achievements)
            showView(achievementsCardView)
        } else {
            achievementsCardView.visibility = View.GONE
        }
    }

    /**
     * GraphView was clicked
     */
    override fun onDateTapped(date: Date) {
        // TODO write implementation.
    }

    private fun customizeChart() {
        val desc = Description()
        desc.text = ""
        lineChartAchievements.description = desc
        lineChartAchievements.axisRight.isEnabled = false
        lineChartAchievements.xAxis.position = XAxis.XAxisPosition.BOTTOM

        lineChartAchievements.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val date = Date(value.toLong())
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
                return sdf.format(date)
            }
        }

        lineChartAchievements.axisLeft.valueFormatter = object : LargeValueFormatter() {}
        lineChartAchievements.legend.setDrawInside(false)

        lineChartAchievements.isHighlightPerTapEnabled = true
        lineChartAchievements.setDrawMarkers(true)
        lineChartAchievements.setTouchEnabled(true)

        lineChartAchievements.isAutoScaleMinMaxEnabled = true
    }

    private fun setChartData(chart: LineChart, achievements: List<Achievement>) {
        val achievedEntries = ArrayList<Entry>()

        achievements
            .filter {
                it.achieved &&
                        it.unlockTime != Date() &&
                        it.unlockTime?.after(AchievementsGraphViewUtil.steamReleaseDate) == true
            }
            .sortedBy { it.unlockTime }
            .map { achievement -> achievedEntries.addEntry(achievements, achievement) }

        val dataSets: MutableList<ILineDataSet> = ArrayList()

        val achievementsDataSet = LineDataSet(achievedEntries, "Completion")
            .customizeDataSet(achievedEntries.size, chart)

        achievementsDataSet.setDrawHighlightIndicators(false)

        dataSets.add(achievementsDataSet)

        val lineData = LineData(dataSets)
        chart.data = lineData
        chart.postInvalidate()

        showView(card_view_progress)
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

    private fun toHours(time: Int, context: Context? = null): String {
        val hours = time / 60
        val minutes = time % 60
        var hoursAbbr = "h"
        var minAbbr = "m"
        if (context != null) {
            hoursAbbr = context.getString(R.string.abbr_hours)
            minAbbr = context.getString(R.string.abbr_minutes)
        }

        return if (hours <= 0 && minutes <= 0) {
            return ""
        } else if (hours <= 0) {
            "$minutes$minAbbr"
        } else {
            "$hours$hoursAbbr, $minutes$minAbbr"
        }
    }
}
