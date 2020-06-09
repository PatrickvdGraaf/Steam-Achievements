package com.crepetete.steamachievements.presentation.fragment.achievements

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.presentation.SteamAchievementsApp
import com.crepetete.steamachievements.presentation.common.adapter.HorizontalAchievementsAdapter
import com.crepetete.steamachievements.presentation.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.presentation.common.view.CircularProgressBar
import com.jjoe64.graphview.GraphView
import java.text.DecimalFormat

class AchievementsFragment : Fragment(), HorizontalAchievementsAdapter.OnAchievementClickListener {
    // TODO decide whether to implement this or make the adapter accept null as listener.
    override fun onAchievementClick(index: Int, sortedList: List<Achievement>) {
        // No implementation yet.
    }

    companion object {
        const val TAG = "ACHIEVEMENTS_FRAGMENT"
        private const val KEY_PLAYER_ID = "KEY_PLAYER_ID"

        fun getInstance(playerId: String): Fragment {
            return AchievementsFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_PLAYER_ID, playerId)
                }
            }
        }
    }

    private lateinit var textViewAllAchievements: TextView
    private lateinit var textViewCompletion: TextView
    private lateinit var bestDayTextView: TextView
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var recyclerViewLatestAchievements: RecyclerView

    private val achievementsAdapter by lazy {
        HorizontalAchievementsAdapter(this)
    }

    private var achievementCount = 0
    private var completionPercentage = 0.0

    private var achievements = listOf<Achievement>()
    private var allAchievements = listOf<Achievement>()

    // Achievements over Time Graph
    private lateinit var achievementsOverTimeGraph: GraphView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
            R.layout.fragment_achievements, container,
            false
        )

        textViewAllAchievements = view.findViewById(R.id.textview_total_achievements)
        textViewCompletion = view.findViewById(R.id.textview_completion)
        circularProgressBar = view.findViewById(R.id.custom_progressBar)
        bestDayTextView = view.findViewById(R.id.best_day_textView)
        achievementsOverTimeGraph = view.findViewById(R.id.lineChartAchievements)

        circularProgressBar.addListener(ValueAnimator.AnimatorUpdateListener {
            updatePercentageText(it.animatedValue as Float)
        })

        recyclerViewLatestAchievements = view.findViewById(R.id.recyclerViewAchievements)
        recyclerViewLatestAchievements.adapter = achievementsAdapter
        recyclerViewLatestAchievements.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL, false
        )

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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (activity!!.application as SteamAchievementsApp).appComponent.inject(this)
    }

    /**
     * Shows the total amount of emptyAchievements.
     */
    private fun setTotalAchievementsInfo(achievementCount: Int) {
        textViewAllAchievements.text = "$achievementCount"
        this.achievementCount = achievementCount
    }

    /**
     * Shows total completion percentage.
     */
    private fun setCompletionPercentage(percentage: Double) {
        completionPercentage = percentage
        circularProgressBar.setProgressWithAnimation(percentage.toFloat())
    }

    /**
     * Shows the users latest emptyAchievements in the RecyclerView and the graph.
     */
    private fun showLatestAchievements(
        achievements: List<Achievement>,
        allAchievements: List<Achievement>
    ) {
        this.achievements = achievements
        this.allAchievements = allAchievements
        achievementsAdapter.setAchievements(achievements)

        AchievementsGraphViewUtil.setAchievementsOverTime(
            achievementsOverTimeGraph,
            allAchievements
        )
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
        textViewCompletion.text = String.format(
            getString(R.string.percentage),
            DecimalFormat(pattern).format(percentage)
        )
    }
}
