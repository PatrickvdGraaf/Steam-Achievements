package com.crepetete.steamachievements.ui.activity.achievements.pager

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
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

/**
 * Activity which holds a ViewPager that shows an achievement.
 */
class TransparentPagerActivity : AppCompatActivity(), Injectable, HasSupportFragmentInjector {

    companion object {
        const val INTENT_KEY_NAME = "INTENT_KEY_NAME"
        const val INTENT_KEY_APP_ID = "INTENT_KEY_APP_ID"
        const val INTENT_KEY_INDEX = "INTENT_KEY_INDEX"
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
        // Get ViewModel and observe.
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(TransparentPagerViewModel::class.java)

        // Use Handler because of https://stackoverflow.com/a/33493282/10074409.
        viewModel.index.observe(this, Observer {
            it?.let { it1 ->
                Handler().postDelayed({ pager.currentItem = it1 }, 100)
            }
        })
        viewModel.achievementData.observe(this, Observer {
            it?.let { it1 -> pagerAdapter.updateAchievements(it1) }
        })

        // Get data from intent.
        if (intent != null) {
            val index = intent?.getIntExtra(INTENT_KEY_INDEX, 0) ?: 0
            viewModel.setIndex(index)

            val names = intent?.getStringArrayListExtra(INTENT_KEY_NAME)
            val appIds = intent?.getStringArrayListExtra(INTENT_KEY_APP_ID)
            if (names != null && appIds != null) {
                viewModel.setAchievementData(names, appIds)
            }
        }

        // Set adapter first.
        pager.adapter = pagerAdapter

        // Set ViewPager settings.
        pager.setPageTransformer(true, ZoomOutPageTransformer())
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = dispatchingAndroidInjector
}