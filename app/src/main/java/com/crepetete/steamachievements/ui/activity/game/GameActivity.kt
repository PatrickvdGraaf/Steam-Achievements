package com.crepetete.steamachievements.ui.activity.game

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.ActivityGameBinding
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.activity.BaseActivity
import com.crepetete.steamachievements.ui.activity.achievements.pager.TransparentPagerActivity
import com.crepetete.steamachievements.ui.common.adapter.HorizontalAchievementsAdapter
import com.crepetete.steamachievements.ui.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.ui.common.graph.point.OnGraphDateTappedListener
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.GameData
import com.crepetete.steamachievements.vo.GameWithAchievements
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*
import javax.inject.Inject

class GameActivity : BaseActivity(), Injectable, OnGraphDateTappedListener, HorizontalAchievementsAdapter.OnAchievementClickListener {

    companion object {
        private const val INTENT_GAME_ID = "INTENT_GAME_ID"
        private const val INTENT_GAME = "INTENT_GAME"
        private const val INTENT_PALETTE = "INTENT_PALETTE"

        private const val INVALID_ID = "-1"

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

    private val achievementsAdapter by lazy { HorizontalAchievementsAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate view and obtain an instance of the binding class.
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game)

        // Set status bar tint.
        setTranslucentStatusBar()

        // Prepare view.
        setContentView(binding.root)
        setSupportActionBar(toolbar)

        // Init view model
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GameViewModel::class.java)

        // Retrieve data.
        intent.getParcelableExtra<GameWithAchievements>(INTENT_GAME)?.let { game ->
            setGameInfo(game)
            viewModel.setGame(game)
        }

        // Set observers
        viewModel.game.observe(this, Observer { game ->
            if (game?.data != null) {
                setGameInfo(game.data)
            }
        })

        /* Update the achievement adapter sorting method.*/
        viewModel.getAchievementSortingMethod().observe(this, Observer { method ->
            /* Update label. */
            sortMethodDescription.text = String.format("Sorted by: %s", method.getName(resources))

            /* Sort achievements in adapter. */
            achievementsAdapter.updateSortingMethod(method)
        })

        // Set Button Listeners.
        sortAchievementsButton.setOnClickListener {
            viewModel.setAchievementSortingMethod()
        }
    }

    override fun onAchievementClick(index: Int, sortedList: List<Achievement>) {
        startActivity(TransparentPagerActivity.getInstance(this, index, sortedList))
    }

    private fun setGameInfo(game: GameWithAchievements?) {
        if (game == null) {
            return
        }

        // Move data into binding.
        val data = GameData(game)
        binding.gameData = data

        // Set Toolbar Title.
        collapsingToolbar.title = game.getName()
        //        title = game.getName()

        // TODO find a way to implement this inside xml with data binding.
        if (data.getRecentPlaytimeString() != "0m") {
            binding.recentlyPlayedTextView.setText(data.getRecentPlaytimeString())
        } else {
            binding.recentlyPlayedTextView.visibility = View.GONE
        }

        binding.totalPlayedTextView.setText(data.getTotalPlayTimeString())

        // Load Banner
        Glide.with(this)
            .asBitmap()
            .load(data.getImageUrl())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?,
                                          model: Any?,
                                          target: Target<Bitmap>?,
                                          isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(resource: Bitmap?,
                                             model: Any?,
                                             target: Target<Bitmap>?,
                                             dataSource: DataSource?,
                                             isFirstResource: Boolean): Boolean {
                    if (resource != null) {
                        Palette.from(resource).generate { palette ->
                            if (palette != null) {
                                val colorBackground = palette.getDarkMutedColor(
                                    ContextCompat.getColor(applicationContext, R.color.colorPrimaryLight)
                                )
                                val colorToolbar = palette.getDarkVibrantColor(
                                    ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                                )
                                val colorCards = if (palette.getMutedColor(ContextCompat.getColor(applicationContext, R.color.colorPrimaryLight)) != ContextCompat.getColor(applicationContext, R.color.colorPrimaryLight)) {
                                    palette.getMutedColor(
                                        ContextCompat.getColor(applicationContext, R.color.colorPrimaryLight)
                                    )
                                } else {
                                    palette.getDarkMutedColor(
                                        ContextCompat.getColor(applicationContext, R.color.colorPrimaryLight)
                                    )
                                }

                                collapsingToolbar.setContentScrimColor(colorToolbar)
                                collapsingToolbar.setStatusBarScrimColor(colorToolbar)

                                val vibrantRgb = palette.darkVibrantSwatch?.rgb
                                val mutedRgb = palette.darkMutedSwatch?.rgb

                                when {
                                    mutedRgb != null -> mutedRgb
                                    vibrantRgb != null -> vibrantRgb
                                    else -> ContextCompat.getColor(binding.root.context,
                                        R.color.colorGameViewHolderTitleBackground)
                                }.let { color ->
                                    playTimeContainer.setBackgroundColor(color)
                                    achievementContainer.setBackgroundColor(color)
                                    progressContainer.setBackgroundColor(color)
                                }

                            }
                        }
                    }
                    return false
                }
            }).into(banner)

        // Prepare Achievements RecyclerView.
        recyclerViewAchievements.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false)

        // Set RecyclerView adapter.
        recyclerViewAchievements.adapter = achievementsAdapter
        recyclerViewAchievements.setHasFixedSize(true)

        // Move achievements to adapter.
        achievementsAdapter.setAchievements(game.achievements)

        // Init Graph.
        AchievementsGraphViewUtil.setAchievementsOverTime(graph, game.achievements, this)
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
}