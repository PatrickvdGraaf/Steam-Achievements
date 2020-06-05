package com.crepetete.steamachievements.ui.activity.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.SteamAchievementsApp
import com.crepetete.steamachievements.api.response.news.NewsItem
import com.crepetete.steamachievements.databinding.ActivityGameBinding
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.activity.BaseActivity
import com.crepetete.steamachievements.ui.activity.achievements.pager.TransparentPagerActivity
import com.crepetete.steamachievements.ui.common.adapter.HorizontalAchievementsAdapter
import com.crepetete.steamachievements.ui.common.adapter.NewsAdapter
import com.crepetete.steamachievements.ui.common.adapter.callback.OnNewsItemClickListener
import com.crepetete.steamachievements.ui.common.graph.point.OnGraphDateTappedListener
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameData
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.activity_game.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Shows a more detailed overview of the available information of a [Game] and its [Achievement]s.
 */
class GameActivity : BaseActivity(), Injectable, OnGraphDateTappedListener,
    HorizontalAchievementsAdapter.OnAchievementClickListener {

    fun getTheSixPreviousDays(dateFormat: String, addToday: Boolean): MutableList<String> {
        val calendar = Calendar.getInstance()
        val currentDate: Date = calendar.time
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val formattedDate = formatter.format(currentDate)
        calendar.time = formatter.parse(formattedDate)!!

        val list: MutableList<String> = ArrayList()
        if (addToday) {
            calendar.add(Calendar.DATE, 0)
            list.add(formatter.format(calendar.time))
            Timber.i(formatter.format(calendar.time))
        }
        for (i in 1..6) {
            calendar.add(Calendar.DATE, -1)
            list.add(formatter.format(calendar.time))
            Timber.i(formatter.format(calendar.time))
        }
        return list
    }

    companion object {
        private const val INTENT_GAME = "INTENT_GAME"

        fun getInstance(context: Context, game: Game): Intent {
            return Intent(context, GameActivity::class.java).apply {
                putExtra(INTENT_GAME, game)
            }
        }
    }

    private lateinit var binding: ActivityGameBinding

    @Inject
    lateinit var viewModel: GameViewModel

    private val newsAdapter by lazy {
        NewsAdapter(object : OnNewsItemClickListener {
            override fun onNewsItemSelected(item: NewsItem) {
                Timber.d(item.gid)
                // TODO show NewsPage.
            }
        })
    }

    private val achievementsAdapter by lazy { HorizontalAchievementsAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as SteamAchievementsApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)

        // Inflate view and obtain an instance of the binding class.
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game)

        // Set status bar tint.
        setTranslucentStatusBar()

        // Prepare view.
        setContentView(binding.root)
        setSupportActionBar(toolbar)

        recycler_view_news.adapter = newsAdapter
        recycler_view_news.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        )

        // Retrieve data.
        intent.getParcelableExtra<Game>(INTENT_GAME)?.let { game ->
            collapsingToolbar.setContentScrimColor(game.getPrimaryColor())
            updateNavigationBarColor(game.getPrimaryColor())

            viewModel.setGame(game)
        }

        // Set observers
        viewModel.game.observe(this, Observer { game ->
            if (game != null) {
                setGameInfo(game)
            }
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
                newsAdapter.setItems(news)
            }
        })

        // Set Button Listeners.
        sortAchievementsButton.setOnClickListener {
            viewModel.setAchievementSortingMethod()
        }

        viewModel.fetchNews()
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
        binding.gameData = data

        collapsingToolbar.title = game.getName()

        Glide.with(this)
            .load(game.getBannerUrl())
            .into(banner)

        // TODO find a way to implement this inside xml with data binding.
        if (data.getRecentPlaytimeString() != "0m") {
            binding.textViewRecentlyPlayed.setText(data.getRecentPlaytimeString())
        } else {
            binding.textViewRecentlyPlayed.visibility = View.GONE
        }

        binding.totalPlayedTextView.setText(data.getTotalPlayTimeString())

        // Prepare Achievements RecyclerView.
        recyclerViewAchievements.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        // Set RecyclerView adapter.
        recyclerViewAchievements.adapter = achievementsAdapter
        recyclerViewAchievements.setHasFixedSize(true)

        // Move achievements to adapter.
        achievementsAdapter.setAchievements(game.achievements)

        // Init Graph.
        customizeChart()
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
}