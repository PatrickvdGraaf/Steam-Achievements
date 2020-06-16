package com.crepetete.steamachievements.presentation.activity.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.presentation.activity.BaseActivity
import com.crepetete.steamachievements.presentation.activity.achievements.TransparentPagerActivity
import com.crepetete.steamachievements.presentation.activity.news.NewsDetailActivity
import com.crepetete.steamachievements.presentation.common.adapter.HorizontalAchievementsAdapter
import com.crepetete.steamachievements.presentation.common.adapter.NewsAdapter
import com.crepetete.steamachievements.presentation.common.adapter.callback.OnNewsItemClickListener
import com.crepetete.steamachievements.presentation.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.presentation.common.graph.point.OnGraphDateTappedListener
import com.crepetete.steamachievements.presentation.vo.GameData
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
import kotlinx.android.synthetic.main.activity_game.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

/**
 * Shows a more detailed overview of the available information of a [Game] and its [Achievement]s.
 */
class GameActivity : BaseActivity(), OnGraphDateTappedListener,
    HorizontalAchievementsAdapter.OnAchievementClickListener {

    companion object {
        private const val INTENT_GAME = "INTENT_GAME"

        fun getInstance(context: Context, game: Game): Intent {
            return Intent(context, GameActivity::class.java).apply {
                putExtra(INTENT_GAME, game)
            }
        }
    }

    private val viewModel: GameViewModel by viewModel()

    private val newsAdapter by lazy {
        NewsAdapter(object : OnNewsItemClickListener {
            override fun onNewsItemSelected(item: NewsItem) {
                startActivity(NewsDetailActivity.getIntent(applicationContext, item))
            }
        })
    }

    private val achievementsAdapter by lazy { HorizontalAchievementsAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set status bar tint.
        setTranslucentStatusBar()
        setContentView(R.layout.activity_game)
        setSupportActionBar(toolbar)

        setViewModelObservers()

        // Retrieve data.
        intent.getParcelableExtra<Game>(INTENT_GAME)?.let { game ->
            collapsingToolbar.setContentScrimColor(game.getPrimaryColor())
            updateNavigationBarColor(game.getPrimaryColor())

            viewModel.setGame(game)
            setGameInfo(game)
        }

        // Set Button Listeners.
        sortAchievementsButton.setOnClickListener {
            viewModel.setAchievementSortingMethod()
        }

        // Prepare RecyclerView.
        recyclerViewAchievements.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        recyclerViewNews.layoutManager = LinearLayoutManager(
            this,
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
        viewModel.game.observe(this, Observer { game ->
            if (game != null) {
                setGameInfo(game)
            }
        })

        viewModel.achievements.observe(this, Observer { achievements ->
            setAchievementsInfo(achievements)
        })

        /* Update the achievement adapter sorting method.*/
        viewModel.getAchievementSortingMethod().observe(this, Observer { method ->
            /* Update label. */
            sortMethodDescription.text = String.format("Sorted by: %s", method.getName(resources))

            /* Sort achievements in adapter. */
            achievementsAdapter.updateSortingMethod(method)
        })

        viewModel.news.observe(this, Observer { nullableNews ->
            nullableNews?.let { news ->
                if (news.isNotEmpty()) {
                    newsAdapter.setItems(news)
                    showView(card_view_news)
                }
            }
        })

        viewModel.newsLoadingState.observe(this, Observer {
            Timber.d("New Loading state: $it.")
        })

        viewModel.newsLoadingError.observe(this, Observer {
            Timber.e("Loading News Failed: ${it?.localizedMessage}")
        })
    }

    private fun showView(contentView: View?) {
        contentView?.apply {
            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .setDuration(400)
                .setListener(null)
        }
    }

    override fun onAchievementClick(index: Int, sortedList: List<Achievement>) {
        startActivity(TransparentPagerActivity.getInstance(this, index, sortedList))
    }

    private fun updateNavigationBarColor(primaryColor: Int) {
        collapsingToolbar.setContentScrimColor(primaryColor)
        collapsingToolbar.setStatusBarScrimColor(primaryColor)
    }

    private fun setGameInfo(game: Game?) {
        if (game == null) {
            return
        }

        // Move data into binding.
        val data = GameData(game)
        collapsingToolbar.title = game.getName()

        Glide.with(this)
            .load(game.getBannerUrl())
            .into(banner)

        if (data.hasTotalPlayTime()) {
            // TODO find a way to implement this inside xml with data binding.
            if (data.getRecentPlaytimeString() != "0m") {
                textViewRecentlyPlayed.setText(data.getRecentPlaytimeString())
            } else {
                textViewRecentlyPlayed.visibility = View.GONE
            }

            totalPlayedTextView.setText(data.getTotalPlayTimeString())

            if (data.hasRecentPlaytime()) {
                textViewRecentlyPlayed.visibility = View.VISIBLE
                textViewRecentlyPlayed.setText(data.getRecentPlaytimeString())
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
}
