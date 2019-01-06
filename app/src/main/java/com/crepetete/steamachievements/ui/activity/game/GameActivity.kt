package com.crepetete.steamachievements.ui.activity.game

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.ActivityGameBinding
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.ui.common.graph.point.OnGraphDateTappedListener
import com.crepetete.steamachievements.ui.view.achievement.adapter.AchievSortingMethod
import com.crepetete.steamachievements.ui.view.achievement.adapter.HorizontalAchievementsAdapter
import com.crepetete.steamachievements.ui.view.component.ValueWithLabelTextView
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.jjoe64.graphview.GraphView
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*
import javax.inject.Inject

private const val INTENT_GAME_ID = "gameId"
fun Activity.startGameActivity(appId: String, imageView: ImageView) {
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
        imageView as View, "banner")
    startActivity(Intent(this, GameActivity::class.java).apply {
        putExtra(INTENT_GAME_ID, appId)
    }, options.toBundle())
}

class GameActivity : AppCompatActivity(), Injectable, OnGraphDateTappedListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: GameViewModel

    private val banner: ImageView by lazy { findViewById<ImageView>(R.id.banner) }
    private val toolBar: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val collapsingToolbarLayout by lazy { findViewById<CollapsingToolbarLayout>(R.id.main_collapsing) }

    // Recently Played
    private val recentlyPlayedTextView by lazy { findViewById<ValueWithLabelTextView>(R.id.recently_played_textView) }

    // Total Playtime
    private val totalPlayedTextView by lazy { findViewById<ValueWithLabelTextView>(R.id.total_played_textView) }

    // Achievements
    private val recyclerViewLatestAchievements by lazy { findViewById<RecyclerView>(R.id.latest_achievements_recyclerview) }
    private val achievementsAdapter by lazy { HorizontalAchievementsAdapter() }
    private val sortAchievementsButton by lazy { findViewById<Button>(R.id.button_sort_achievements) }
    private val sortMethodDescription by lazy { findViewById<TextView>(R.id.sorting_textview) }

    // Achievements over Time Graph
    private val achievementsOverTimeGraph by lazy { findViewById<GraphView>(R.id.graph) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate view and obtain an instance of the binding class.
        val binding: ActivityGameBinding = DataBindingUtil.setContentView(this,
            R.layout.activity_game)

        // Specify the current activity as the lifecycle owner.
        binding.setLifecycleOwner(this)

        setContentView(binding.root)
        setSupportActionBar(toolBar)

        val appId = intent.getStringExtra(INTENT_GAME_ID)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameViewModel::class.java)
        viewModel.setAppId(appId)
        viewModel.game.observe(this, Observer { game ->
            setGameInfo(game)
        })

        viewModel.finalAchievements.observe(this, Observer { resource ->
            val data = resource?.data
            if (data != null) {
                setAchievements(data.filter {
                    it.appId == appId
                })
            }
        })


        viewModel.vibrantColor.observe(this, Observer { swatch ->
            if (swatch != null) {
                setCollapsingToolbarColors(swatch.rgb)
            }
        })

        viewModel.mutedColor.observe(this, Observer { swatch ->
            if (swatch != null) {
                playtime_header_textview.setTextColor(swatch.bodyTextColor)
                scrollView.setBackgroundColor(swatch.rgb)
            }
        })

        sortAchievementsButton.setOnClickListener {
            setSortingMethod()
        }
    }

    private fun updateSortMethodText(description: String) {
        sortMethodDescription.text = "Sorted by: $description"
    }

    private fun setGameInfo(game: GameWithAchievements?) {
        if (game == null) {
            return
        }

        recentlyPlayedTextView.setText(game.getRecentPlaytimeString())
        totalPlayedTextView.setText(game.getTotalPlayTimeString())

        recyclerViewLatestAchievements.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,
            false)

        recyclerViewLatestAchievements.adapter = achievementsAdapter

        Glide.with(this)
            .load(game.getFullLogoUrl())
            .into(object : SimpleTarget<Drawable>() {
                /**
                 * The method that will be called when the resource load has finished.
                 *
                 * @param resource the loaded resource.
                 */
                override fun onResourceReady(resource: Drawable,
                                             transition: Transition<in Drawable>?) {
                    if (resource is BitmapDrawable) {
                        Palette.from(resource.bitmap).generate {
                            it?.let { it1 -> viewModel.updatePalette(it1) }
                            banner.setImageDrawable(resource)
                        }
                    }
                }
            })

        collapsingToolbarLayout.title = game.getName()
        title = game.getName()

        setCollapsingToolbarColors(game.getPrimaryColor())
    }

    private fun setAchievements(achievements: List<Achievement>) {
        achievementsAdapter.setAchievements(achievements)

        AchievementsGraphViewUtil.setAchievementsOverTime(achievementsOverTimeGraph, achievements,
            this)
    }

    /**
     * GraphView was clicked
     */
    override fun onDateTapped(date: Date) {
        val achievements = viewModel.finalAchievements.value?.data?.filter {
            it.achieved
        }

        if (achievements != null) {
            setSortingMethod(AchievSortingMethod.NOT_ACHIEVED)

            val calTapped = Calendar.getInstance()
            calTapped.time = date
            achievements.forEachIndexed { index, achievement ->
                val calAchievement = Calendar.getInstance()
                calAchievement.time = achievement.unlockTime

                if (calAchievement.get(Calendar.YEAR) == calTapped.get(Calendar.YEAR)
                    && calAchievement.get(Calendar.MONTH) == calTapped.get(Calendar.MONTH)
                    && calAchievement.get(Calendar.DAY_OF_MONTH) == calTapped.get(
                        Calendar.DAY_OF_MONTH)) {
                    recyclerViewLatestAchievements.smoothScrollToPosition(achievements.size
                        - index)
                }
            }
        }
    }

    private fun setSortingMethod(sortingMethod: AchievSortingMethod? = null) {
        val desc = achievementsAdapter.updateSortingMethod(sortingMethod)
        updateSortMethodText(desc)
    }

    private fun setCollapsingToolbarColors(@ColorInt color: Int) {
        setTranslucentStatusBar()

        collapsingToolbarLayout.setContentScrimColor(color)
        collapsingToolbarLayout.setStatusBarScrimColor(color)
    }

    private fun setTranslucentStatusBar(color: Int = ContextCompat.getColor(window.context,
        R.color.statusbar_translucent)) {
        val sdkInt = Build.VERSION.SDK_INT
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentStatusBarLollipop(color)
        } else if (sdkInt >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatusBarKiKat(window)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setTranslucentStatusBarLollipop(color: Int = ContextCompat.getColor(window.context,
        R.color.statusbar_translucent)) {
        window.statusBarColor = color
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun setTranslucentStatusBarKiKat(window: Window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
}