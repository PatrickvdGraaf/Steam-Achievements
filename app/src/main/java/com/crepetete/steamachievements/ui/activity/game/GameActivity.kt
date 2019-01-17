package com.crepetete.steamachievements.ui.activity.game

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.ActivityGameBinding
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.ui.common.graph.point.OnGraphDateTappedListener
import com.crepetete.steamachievements.ui.view.achievement.adapter.AchievSortingMethod
import com.crepetete.steamachievements.ui.view.achievement.adapter.HorizontalAchievementsAdapter
import com.crepetete.steamachievements.vo.GameData
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.jjoe64.graphview.GraphView
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*
import javax.inject.Inject

class GameActivity : AppCompatActivity(), Injectable, OnGraphDateTappedListener {
    companion object {
        private const val INTENT_GAME_ID = "INTENT_GAME_ID"
        private const val INTENT_GAME = "INTENT_GAME"

        private const val INVALID_ID = "-1"

        fun getInstance(context: Context, appId: String): Intent {
            return Intent(context, GameActivity::class.java).apply {
                putExtra(INTENT_GAME_ID, appId)
            }
        }

        fun getInstance(context: Context, game: GameWithAchievements): Intent {
            return Intent(context, GameActivity::class.java).apply {
                putExtra(INTENT_GAME, game)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var binding: ActivityGameBinding

    private lateinit var viewModel: GameViewModel

    private val achievementsAdapter by lazy { HorizontalAchievementsAdapter() }

    // Achievements over Time Graph
    private val achievementsOverTimeGraph by lazy { findViewById<GraphView>(R.id.graph) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate view and obtain an instance of the binding class.
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game)

        setContentView(binding.root)
        setSupportActionBar(toolbar)

        // Init view model
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameViewModel::class.java)

        // Retrieve data.
        val id = intent.getStringExtra(INTENT_GAME_ID) ?: INVALID_ID
        if (id != INVALID_ID) {
            viewModel.setAppId(id)
        } else {
            intent.getParcelableExtra<GameWithAchievements>(INTENT_GAME)?.let { game ->
                setGameInfo(game)
                viewModel.setGame(game)
            }
        }

        // Set observers
        viewModel.game.observe(this, Observer { game ->
            if (game?.data != null) {
                setGameInfo(game.data)
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

    private fun setGameInfo(game: GameWithAchievements) {
        val data = GameData(game)
        binding.gameData = data

        if (game.getPrimaryColor() <= 0) {
            Glide.with(this)
                .load(data.getImageUrl())
                .into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable,
                                                 transition: Transition<in Drawable>?) {
                        if (resource is BitmapDrawable) {
                            Palette.from(resource.bitmap).generate {
                                // Listener should update the database, which will trigger LiveData observers,
                                // and the view should reload with the new background color.
                                if (it != null) {
                                    viewModel.updatePalette(it)
                                }
                            }
                        }

                        banner.setImageDrawable(resource)
                    }
                })
        } else {
            Glide.with(this)
                .load(data.getImageUrl())
                .into(banner)
        }

        recyclerViewLatestAchievements.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL,
            false)

        achievementsAdapter.setAchievements(game.achievements)
        AchievementsGraphViewUtil.setAchievementsOverTime(achievementsOverTimeGraph, game.achievements,
            this)

        recyclerViewLatestAchievements.adapter = achievementsAdapter


        if (game.getPrimaryColor() == 0) {
            updateBackgroundColorFromBanner(binding.root.context, data.getImageUrl(), game.getAppId())
        } else {
            binding.scrollView.setBackgroundColor(game.getPrimaryColor())
        }

        collapsingToolbar.title = game.getName()
        title = game.getName()

        setCollapsingToolbarColors(game.getPrimaryColor())
    }

    private fun updateBackgroundColorFromBanner(context: Context, url: String, appId: String) {
        Glide.with(context)
            .load(url)
            .priority(Priority.LOW)
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable,
                                             transition: Transition<in Drawable>?) {
                    if (resource is BitmapDrawable) {
                        Palette.from(resource.bitmap).generate {
                            val vibrantRgb = it?.darkVibrantSwatch?.rgb
                            val mutedRgb = it?.darkMutedSwatch?.rgb
                            val defaultBackgroundColor = ContextCompat.getColor(context,
                                R.color.colorGameViewHolderTitleBackground)

                            val rgb = when {
                                mutedRgb != null -> mutedRgb
                                vibrantRgb != null -> vibrantRgb
                                else -> defaultBackgroundColor
                            }

                            // Listener should update the database, which will trigger LiveData observers,
                            // and the view should reload with the new background color.
                            binding.scrollView.setBackgroundColor(rgb)
                        }
                    }
                }
            })
    }

    /**
     * GraphView was clicked
     */
    override fun onDateTapped(date: Date) {
        //        val achievements = viewModel.finalAchievements.value?.data?.filter {
        //            it.achieved
        //        }

        //        if (achievements != null) {
        //            setSortingMethod(AchievSortingMethod.NOT_ACHIEVED)
        //
        //            val calTapped = Calendar.getInstance()
        //            calTapped.time = date
        //            achievements.forEachIndexed { index, achievement ->
        //                val calAchievement = Calendar.getInstance()
        //                calAchievement.time = achievement.unlockTime
        //
        //                if (calAchievement.get(Calendar.YEAR) == calTapped.get(Calendar.YEAR)
        //                    && calAchievement.get(Calendar.MONTH) == calTapped.get(Calendar.MONTH)
        //                    && calAchievement.get(Calendar.DAY_OF_MONTH) == calTapped.get(
        //                        Calendar.DAY_OF_MONTH)) {
        //                    recyclerViewLatestAchievements.smoothScrollToPosition(achievements.size
        //                        - index)
        //                }
        //            }
        //        }
    }

    private fun setSortingMethod(sortingMethod: AchievSortingMethod? = null) {
        val desc = achievementsAdapter.updateSortingMethod(sortingMethod)
        updateSortMethodText(desc)
    }

    private fun setCollapsingToolbarColors(@ColorInt color: Int) {
        setTranslucentStatusBar()

        collapsingToolbar.setContentScrimColor(color)
        collapsingToolbar.setStatusBarScrimColor(color)
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