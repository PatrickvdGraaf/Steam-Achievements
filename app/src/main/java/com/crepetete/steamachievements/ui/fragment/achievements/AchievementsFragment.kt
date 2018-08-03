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
import com.crepetete.steamachievements.ui.activity.helper.LoadingIndicator
import com.crepetete.steamachievements.ui.view.CircularProgressBar
import com.crepetete.steamachievements.ui.view.achievement.adapter.HorizontalAchievementsAdapter
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
        HorizontalAchievementsAdapter(this)
    }

    private var achievementCount = 0
    private var completionPercentage = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_achievements, container,
                false)

        textViewAllAchievements = view.findViewById(R.id.textview_total_achievements)
        textViewCompletion = view.findViewById(R.id.textview_completion)
        circularProgressBar = view.findViewById(R.id.custom_progressBar)
        bestDayTextView = view.findViewById(R.id.best_day_textView)

        circularProgressBar.addListener(ValueAnimator.AnimatorUpdateListener {
            updatePercentageText(it.animatedValue as Float)
        })

        recyclerViewLatestAchievements = view.findViewById(R.id.latest_achievements_recyclerview)

        recyclerViewLatestAchievements.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,
                false)

        recyclerViewLatestAchievements.adapter = achievementsAdapter

        if (achievementCount > 0) {
            setTotalAchievementsInfo(achievementCount)
            setCompletionPercentage(completionPercentage)
        }

        return view
    }

    override fun setTotalAchievementsInfo(achievementCount: Int) {
        textViewAllAchievements.text = "$achievementCount"
        this.achievementCount = achievementCount
    }

    override fun setCompletionPercentage(percentage: Double) {
        completionPercentage = percentage
        circularProgressBar.setProgressWithAnimation(percentage.toFloat())
    }

    override fun showBestDay(day: Pair<String,Int>) {
        bestDayTextView.text = "${day.first}; ${day.second} achievements."
    }

    override fun showLatestAchievements(achievements: List<Achievement>) {
        achievementsAdapter.setAchievements(achievements)
    }

    override fun instantiatePresenter(): AchievementPresenter {
        return AchievementPresenter(this)
    }

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
