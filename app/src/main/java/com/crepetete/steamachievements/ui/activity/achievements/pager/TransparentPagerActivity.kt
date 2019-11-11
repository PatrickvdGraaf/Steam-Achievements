package com.crepetete.steamachievements.ui.activity.achievements.pager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.SteamAchievementsApp
import com.crepetete.steamachievements.ui.activity.BaseActivity
import com.crepetete.steamachievements.ui.activity.achievements.pager.transformer.ZoomOutPageTransformer
import com.crepetete.steamachievements.ui.activity.game.fragment.achievement.pager.adapter.ScreenSlidePagerAdapter
import com.crepetete.steamachievements.vo.Achievement
import kotlinx.android.synthetic.main.activity_pager.*
import javax.inject.Inject

/**
 * Activity which holds a ViewPager that shows an achievement.
 */
class TransparentPagerActivity : BaseActivity() {

    companion object {
        private const val INTENT_KEY_ACHIEVEMENT = "INTENT_KEY_ACHIEVEMENT"
        private const val INTENT_KEY_INDEX = "INTENT_KEY_INDEX"

        fun getInstance(context: Context, index: Int, achievements: List<Achievement>): Intent {
            return Intent(context, TransparentPagerActivity::class.java).apply {
                putExtra(INTENT_KEY_INDEX, index)
                putParcelableArrayListExtra(INTENT_KEY_ACHIEVEMENT, ArrayList(achievements))
            }
        }
    }

    @Inject
    lateinit var viewModel: TransparentPagerViewModel

    private val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as SteamAchievementsApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)

        // Set status bar tint.
        setTranslucentStatusBar()

        // Set adapter first.
        pager.adapter = pagerAdapter

        // Set ViewPager settings.
        pager.setPageTransformer(true, ZoomOutPageTransformer())

        viewModel.achievementData.observe(this, Observer {
            pagerAdapter.updateAchievements(it)
        })

        // Get data from intent.
        if (intent != null) {
            viewModel.setAchievementData(intent.getParcelableArrayListExtra(INTENT_KEY_ACHIEVEMENT))
            viewModel.setIndex(intent.getIntExtra(INTENT_KEY_INDEX, 0))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.index.observe(this, Observer { index ->
            pager.setCurrentItem(index, false)
        })
    }
}