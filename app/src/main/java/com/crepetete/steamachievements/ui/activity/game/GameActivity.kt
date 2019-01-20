package com.crepetete.steamachievements.ui.activity.game

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
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
import com.crepetete.steamachievements.ui.common.adapter.HorizontalAchievementsAdapter
import com.crepetete.steamachievements.ui.common.enums.AchievSortingMethod
import com.crepetete.steamachievements.ui.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.ui.common.graph.point.OnGraphDateTappedListener
import com.crepetete.steamachievements.vo.GameData
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.jjoe64.graphview.GraphView
import kotlinx.android.synthetic.main.activity_game.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class GameActivity : AppCompatActivity(), Injectable, OnGraphDateTappedListener {
    companion object {
        private const val INTENT_GAME_ID = "INTENT_GAME_ID"
        private const val INTENT_GAME = "INTENT_GAME"

        private const val INVALID_ID = "-1"

        fun getInstance(context: Context, appId: String) = Intent(
            context,
            GameActivity::class.java
        ).apply {
            putExtra(INTENT_GAME_ID, appId)
        }

        fun getInstance(context: Context, game: GameWithAchievements) = Intent(
            context,
            GameActivity::class.java
        ).apply {
            putExtra(INTENT_GAME, game)
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

        // Set status bar tint.
        setTranslucentStatusBar()

        // Prepare view.
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
                collapsingToolbar.setContentScrimColor(swatch.rgb)
                collapsingToolbar.setStatusBarScrimColor(swatch.rgb)
            }
        })

        viewModel.mutedColor.observe(this, Observer { swatch ->
            if (swatch != null) {
                scrollView.setBackgroundColor(swatch.rgb)
            }
        })

        // Set Button Listeners.
        sortAchievementsButton.setOnClickListener {
            setSortingMethod()
        }
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

        // Set ScrollView background to game specific color.
        binding.scrollView.setBackgroundColor(game.getPrimaryColor())

        // Load Banner
        Glide.with(this)
            .asBitmap()
            .load(data.getImageUrl())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    Timber.w(e, "Error while loading image from url: ${data.getImageUrl()}.")
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
                                viewModel.updatePalette(palette)
                            }
                        }
                    }
                    return false
                }

            })
            .into(banner)

        // Prepare Achievements RecyclerView.
        recyclerViewLatestAchievements.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false)

        // Set RecyclerView adapter.
        recyclerViewLatestAchievements.adapter = achievementsAdapter
        recyclerViewLatestAchievements.setHasFixedSize(true)

        // Move achievements to adapter.
        achievementsAdapter.setAchievements(game.achievements)

        // Init Graph.
        AchievementsGraphViewUtil.setAchievementsOverTime(
            achievementsOverTimeGraph,
            game.achievements,
            this)
    }

    private fun setSortingMethod(sortingMethod: AchievSortingMethod? = null) {
        sortMethodDescription.text = String.format("Sorted by: %", achievementsAdapter.updateSortingMethod(sortingMethod))
    }

    private fun setTranslucentStatusBar(color: Int = ContextCompat.getColor(window.context, R.color.statusbar_translucent)) {
        val sdkInt = Build.VERSION.SDK_INT
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        } else if (sdkInt >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
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