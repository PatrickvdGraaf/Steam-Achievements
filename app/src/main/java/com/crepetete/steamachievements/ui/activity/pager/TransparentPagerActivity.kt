package com.crepetete.steamachievements.ui.activity.pager

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.ViewPager
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.ui.activity.pager.transformer.ZoomOutPageTransformer
import com.crepetete.steamachievements.ui.fragment.achievement.pager.adapter.ScreenSlidePagerAdapter
import com.crepetete.steamachievements.utils.bind
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Activity which holds a ViewPager that shows an achievement.
 */
class TransparentPagerActivity : DaggerAppCompatActivity() {
    companion object {
        const val INTENT_KEY_NAME = "INTENT_KEY_NAME"
        const val INTENT_KEY_APP_ID = "INTENT_KEY_APP_ID"
        const val INTENT_KEY_INDEX = "INTENT_KEY_INDEX"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

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

            val names = intent?.getSerializableExtra(INTENT_KEY_NAME) as ArrayList<String>?
            val appIds = intent?.getSerializableExtra(INTENT_KEY_APP_ID) as ArrayList<String>?
            if (names != null && appIds != null) {
                viewModel.setAchievementData(names, appIds)
            }
        }

        // Set adapter first.
        pager.adapter = pagerAdapter

        // Set ViewPager settings.
        pager.setPageTransformer(true, ZoomOutPageTransformer())
//        pager.setOnClickListener {
//            onBackPressed()
//        }
    }
}