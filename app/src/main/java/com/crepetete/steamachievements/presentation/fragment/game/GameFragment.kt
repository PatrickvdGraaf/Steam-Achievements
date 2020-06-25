package com.crepetete.steamachievements.presentation.fragment.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
import com.crepetete.steamachievements.presentation.common.graph.point.OnGraphDateTappedListener
import com.crepetete.steamachievements.presentation.fragment.BaseFragment
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
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
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

    private val viewModel: GameViewModel by viewModel()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewModelObservers()

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

        viewModel.graphData.observe(
            viewLifecycleOwner,
            Observer { entries ->
                setChartData(lineChartAchievements, entries)
            }
        )

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
        // Set Top Day text.
        // TODO move to function and/or to ViewModel
        val unlockDays = mutableListOf<Int?>()
        achievements.map {
            it.unlockTime?.let { date ->
                val c: Calendar = Calendar.getInstance()
                c.time = date
                c.get(Calendar.DAY_OF_WEEK)
            }
        }.forEach {
            unlockDays.add(it)
        }
        unlockDays.filterNotNull().maxBy { it }?.let {
            valueViewTopDay.setText("Top day: ${DateFormatSymbols().weekdays[it]}.")
        }


        // Set Achievements container.
        if (achievements.isNotEmpty()) {
            // Move achievements to adapter.
            achievementsAdapter.setAchievements(achievements)
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

    private fun setChartData(chart: LineChart, achievedEntries: ArrayList<Entry>) {
        Glide.with(chart.context)
            .asBitmap()
            .load(viewModel.game.value?.getBannerUrl())
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let { bitmap ->
                        Palette.from(bitmap).generate { palette ->
                            val backgroundColor =
                                palette?.lightMutedSwatch?.rgb ?: ContextCompat.getColor(
                                    chart.context,
                                    R.color.white
                                )

                            val achievementsDataSet = LineDataSet(
                                achievedEntries,
                                "Completion"
                            )

                            customizeDataSet(
                                achievementsDataSet,
                                achievedEntries.size,
                                backgroundColor
                            )

                            val dataSets: MutableList<ILineDataSet> = ArrayList()

                            dataSets.add(achievementsDataSet)

                            val lineData = LineData(dataSets)
                            chart.data = lineData
                            chart.postInvalidate()

                            showView(card_view_progress)
                        }
                    }

                    return false
                }
            }).submit()
    }

    private fun customizeDataSet(
        dateSet: LineDataSet,
        dataSetSize: Int,
        gradientColor: Int? = null
    ) {
        dateSet.setDrawFilled(true)
        dateSet.setDrawValues(false)

        // Line color (?)
        dateSet.color = R.color.colorAccent

        // Background gradient
        gradientColor?.let { gradient ->
            dateSet.fillDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(gradient, Color.TRANSPARENT)
            ).apply { cornerRadius = 0f }
        }

        for (index in 0..dataSetSize - 2) {
            dateSet.circleColors[0] = dateSet.color
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
