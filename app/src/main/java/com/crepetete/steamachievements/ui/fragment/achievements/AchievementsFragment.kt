package com.crepetete.steamachievements.ui.fragment.achievements

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseFragment
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.ui.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.ui.common.helper.LoadingIndicator
import com.crepetete.steamachievements.ui.view.CircularProgressBar
import com.crepetete.steamachievements.ui.view.achievement.adapter.HorizontalAchievementsAdapter
import com.jjoe64.graphview.GraphView
import java.text.DecimalFormat

class AchievementsFragment : BaseFragment<AchievementPresenter>(), AchievementsView {
    companion object {
        const val TAG = "ACHIEVEMENTS_FRAGMENT"
        private const val KEY_PLAYER_ID = "KEY_PLAYER_ID"

        fun getInstance(playerId: String, loadingIndicator: LoadingIndicator): Fragment {
            return AchievementsFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_PLAYER_ID, playerId)
                }
                setLoaderIndicator(loadingIndicator)
            }
        }
    }

    private lateinit var textViewAllAchievements: TextView
    private lateinit var textViewCompletion: TextView
    private lateinit var bestDayTextView: TextView
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var recyclerViewLatestAchievements: RecyclerView

    private val achievementsAdapter by lazy {
        HorizontalAchievementsAdapter()
    }

    private var achievementCount = 0
    private var completionPercentage = 0.0

    private var achievements = listOf<Achievement>()
    private var allAchievements = listOf<Achievement>()

    // Achievements over Time Graph
    private lateinit var achievementsOverTimeGraph: GraphView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_achievements, container,
                false)

        textViewAllAchievements = view.findViewById(R.id.textview_total_achievements)
        textViewCompletion = view.findViewById(R.id.textview_completion)
        circularProgressBar = view.findViewById(R.id.custom_progressBar)
        bestDayTextView = view.findViewById(R.id.best_day_textView)
        achievementsOverTimeGraph = view.findViewById(R.id.graph)

        circularProgressBar.addListener(ValueAnimator.AnimatorUpdateListener {
            updatePercentageText(it.animatedValue as Float)
        })

        recyclerViewLatestAchievements = view.findViewById(R.id.latest_achievements_recyclerview)
        recyclerViewLatestAchievements.adapter = achievementsAdapter
        recyclerViewLatestAchievements.layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (achievementCount > 0) {
            setTotalAchievementsInfo(achievementCount)
            setCompletionPercentage(completionPercentage)
        }

        if (achievements.isNotEmpty()) {
            showLatestAchievements(achievements, allAchievements)
        }
    }

    /**
     * Shows the total amount of achievements.
     */
    override fun setTotalAchievementsInfo(achievementCount: Int) {
        textViewAllAchievements.text = "$achievementCount"
        this.achievementCount = achievementCount
    }

    /**
     * Shows total completion percentage.
     */
    override fun setCompletionPercentage(percentage: Double) {
        completionPercentage = percentage
        circularProgressBar.setProgressWithAnimation(percentage.toFloat())
    }

    /**
     * Shows the date and amount of the day on which the user achieved most of his achievements.
     */
    override fun showBestDay(day: Pair<String, Int>) {
        bestDayTextView.text = "${day.first}; ${day.second} achievements."
    }

    /**
     * Shows the users latest achievements in the RecyclerView and the graph.
     */
    override fun showLatestAchievements(achievements: List<Achievement>,
                                        allAchievements: List<Achievement>) {
        this.achievements = achievements
        this.allAchievements = allAchievements
        achievementsAdapter.setAchievements(achievements)

        AchievementsGraphViewUtil.setAchievementsOverTime(achievementsOverTimeGraph, allAchievements)
    }

    /**
     * Updates the percentage text in the center of the circular ProgressBar
     *
     * TODO make this a custom view.
     */
    private fun updatePercentageText(percentage: Float) {
        val pattern = if (percentage < 100f) {
            "#,###0.000"
        } else {
            "#,###"
        }
        textViewCompletion.text = String.format(getString(R.string.percentage),
                DecimalFormat(pattern).format(percentage))
    }
}
