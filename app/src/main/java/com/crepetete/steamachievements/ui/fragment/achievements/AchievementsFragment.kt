package com.crepetete.steamachievements.ui.fragment.achievements


import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseFragment
import com.crepetete.steamachievements.ui.activity.helper.LoadingIndicator
import com.crepetete.steamachievements.ui.view.CircularProgressBar
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
    private lateinit var circularProgressBar: CircularProgressBar

    private var achievementCount = 0
    private var completionPercentage = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)

        textViewAllAchievements = view.findViewById(R.id.textview_total_achievements)
        textViewCompletion = view.findViewById(R.id.textview_completion)
        circularProgressBar = view.findViewById(R.id.custom_progressBar)

        circularProgressBar.addListener(ValueAnimator.AnimatorUpdateListener {
            updatePercentageText(it.animatedValue as Float)
        })

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
