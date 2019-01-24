package com.crepetete.steamachievements.ui.activity.achievements.pager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.activity.achievements.pager.transformer.ZoomOutPageTransformer
import com.crepetete.steamachievements.ui.fragment.achievement.pager.adapter.ScreenSlidePagerAdapter
import com.crepetete.steamachievements.util.extensions.bind
import com.crepetete.steamachievements.vo.Achievement
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * Activity which holds a ViewPager that shows an achievement.
 */
class TransparentPagerActivity : AppCompatActivity(), Injectable, HasSupportFragmentInjector {

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
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private lateinit var viewModel: TransparentPagerViewModel

    private val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)

    private val pager by bind<ViewPager>(R.id.pager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)

        // Set adapter first.
        pager.adapter = pagerAdapter

        // Set ViewPager settings.
        pager.setPageTransformer(true, ZoomOutPageTransformer())

        // Get ViewModel and observe.
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(TransparentPagerViewModel::class.java)

        // Use Handler because of https://stackoverflow.com/a/33493282/10074409.
        viewModel.index.observe(this, Observer { index ->
            Handler().postDelayed({ pager.setCurrentItem(index, false) }, 100)

        })
        viewModel.achievementData.observe(this, Observer {
            pagerAdapter.updateAchievements(it)
        })

        // Get data from intent.
        if (intent != null) {
            viewModel.setAchievementData(intent.getParcelableArrayListExtra(INTENT_KEY_ACHIEVEMENT))
            viewModel.setIndex(intent.getIntExtra(INTENT_KEY_INDEX, 0))
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector
}