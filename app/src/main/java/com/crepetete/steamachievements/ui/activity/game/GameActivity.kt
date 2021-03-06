package com.crepetete.steamachievements.ui.activity.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.ActivityGameBinding
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.activity.BaseActivity
import com.crepetete.steamachievements.ui.activity.achievements.pager.TransparentPagerActivity
import com.crepetete.steamachievements.ui.common.adapter.HorizontalAchievementsAdapter
import com.crepetete.steamachievements.ui.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.ui.common.graph.point.OnGraphDateTappedListener
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Game
import com.crepetete.steamachievements.vo.GameData
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*
import javax.inject.Inject

class GameActivity : BaseActivity(), Injectable, OnGraphDateTappedListener, HorizontalAchievementsAdapter.OnAchievementClickListener {

    companion object {
        private const val INTENT_GAME_ID = "INTENT_GAME_ID"
        private const val INTENT_GAME = "INTENT_GAME"

        private const val INTENT_PALETTE_DARK_MUTED = "INTENT_PALETTE_DARK_MUTED"
        private const val INTENT_PALETTE_DARK_VIBRANT = "INTENT_PALETTE_DARK_VIBRANT"
        private const val INTENT_PALETTE_LIGHT_MUTED = "INTENT_PALETTE_LIGHT_MUTED"
        private const val INTENT_PALETTE_LIGHT_VIBRANT = "INTENT_PALETTE_LIGHT_VIBRANT"
        private const val INTENT_PALETTE_MUTED = "INTENT_PALETTE_MUTED"
        private const val INTENT_PALETTE_VIBRANT = "INTENT_PALETTE_VIBRANT"
        private const val INTENT_PALETTE_DOMINANT = "INTENT_PALETTE_DOMINANT"

        private const val INVALID_ID = "-1"

        fun getInstance(context: Context, game: Game, palette: Palette?) = Intent(context, GameActivity::class.java)
            .apply {
                putExtra(INTENT_GAME, game)
                putExtra(INTENT_PALETTE_DARK_MUTED, palette?.darkMutedSwatch?.rgb ?: -1)
                putExtra(INTENT_PALETTE_DARK_VIBRANT, palette?.darkVibrantSwatch?.rgb ?: -1)
                putExtra(INTENT_PALETTE_LIGHT_MUTED, palette?.lightMutedSwatch?.rgb ?: -1)
                putExtra(INTENT_PALETTE_LIGHT_VIBRANT, palette?.lightVibrantSwatch?.rgb ?: -1)
                putExtra(INTENT_PALETTE_MUTED, palette?.mutedSwatch?.rgb ?: -1)
                putExtra(INTENT_PALETTE_VIBRANT, palette?.vibrantSwatch?.rgb ?: -1)
                putExtra(INTENT_PALETTE_DOMINANT, palette?.dominantSwatch?.rgb ?: -1)
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
        val id = intent.getStringExtra(INTENT_GAME_ID) ?: INVALID_ID
        if (id != INVALID_ID) {
            viewModel.setAppId(id)
        } else {
            intent.getParcelableExtra<Game>(INTENT_GAME)?.let { game ->
                setGameInfo(game)
                viewModel.setGame(game)
            }
        }

        // Set observers
        viewModel.game.observe(this, Observer { game ->
            if (game != null) {
                setGameInfo(game)
            }
        })

        /* Update the achievement adapter sorting method.*/
        viewModel.getAchievementSortingMethod().observe(this, Observer { method ->
            /* Update label. */
            sortMethodDescription.text = String.format("Sorted by: %s", method.getName(resources))

            /* Sort achievements in adapter. */
            achievementsAdapter.updateSortingMethod(method)
        })

        setColorsWithIntent(intent)

        // Set Button Listeners.
        sortAchievementsButton.setOnClickListener {
            viewModel.setAchievementSortingMethod()
        }
    }

    private fun setColorsWithIntent(intent: Intent?) {
        val darkMuted = intent?.getIntExtra(INTENT_PALETTE_DARK_VIBRANT, -1) ?: -1
        val darkVibrant = intent?.getIntExtra(INTENT_PALETTE_DARK_MUTED, -1) ?: -1
        val lightMuted = intent?.getIntExtra(INTENT_PALETTE_LIGHT_MUTED, -1) ?: -1
        val lightVibrant = intent?.getIntExtra(INTENT_PALETTE_LIGHT_VIBRANT, -1) ?: -1
        val muted = intent?.getIntExtra(INTENT_PALETTE_MUTED, -1) ?: -1
        val vibrant = intent?.getIntExtra(INTENT_PALETTE_VIBRANT, -1) ?: -1
        val dominant = intent?.getIntExtra(INTENT_PALETTE_DOMINANT, -1) ?: -1

        if (darkMuted != -1) {
            collapsingToolbar.setContentScrimColor(darkMuted)
            collapsingToolbar.setStatusBarScrimColor(darkMuted)
        } else if (muted != -1) {
            collapsingToolbar.setContentScrimColor(muted)
            collapsingToolbar.setStatusBarScrimColor(muted)
        }

        when {
            darkVibrant != -1 -> scrollView.setBackgroundColor(darkVibrant)
            lightMuted != -1 -> scrollView.setBackgroundColor(lightMuted)
            muted != -1 -> scrollView.setBackgroundColor(muted)
            dominant != -1 -> scrollView.setBackgroundColor(dominant)
        }
    }

    override fun onAchievementClick(index: Int, sortedList: List<Achievement>) {
        startActivity(TransparentPagerActivity.getInstance(this, index, sortedList))
    }

    private fun setGameInfo(game: Game?) {
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
            .load(data.getImageUrl())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(banner)

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