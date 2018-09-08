package com.crepetete.steamachievements.ui.activity.game

import android.annotation.TargetApi
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.databinding.ActivityGameBinding
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.ui.activity.game.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.ui.view.achievement.adapter.HorizontalAchievementsAdapter
import com.jjoe64.graphview.GraphView
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


private const val INTENT_GAME_ID = "gameId"
fun Activity.startGameActivity(appId: String, imageView: ImageView) {
    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
            imageView as View, "banner")
    startActivity(Intent(this, GameActivity::class.java).apply {
        putExtra(INTENT_GAME_ID, appId)
    }, options.toBundle())
}

class GameActivity : DaggerAppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: GameViewModel

    private val banner: ImageView by lazy { findViewById<ImageView>(R.id.banner) }
    private val toolBar: Toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val collapsingToolbarLayout by lazy { findViewById<CollapsingToolbarLayout>(R.id.main_collapsing) }

    // Recently Played
    private val recentPlayTimeContainer by lazy { findViewById<View>(R.id.container_recent_playtime) }
    private val recentlyPlayedTextView by lazy { findViewById<TextView>(R.id.recently_played_textView) }

    // Total Playtime
    private val totalPlayedTextView by lazy { findViewById<TextView>(R.id.total_played_textView) }

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

        viewModel.achievements.observe(this, Observer {
            setAchievements(it?.data ?: listOf())
        })

        sortAchievementsButton.setOnClickListener {
            val desc = achievementsAdapter.updateSortingMethod()
            updateSortMethodText(desc)
        }
    }

    private fun updateSortMethodText(description: String) {
        sortMethodDescription.text = "Sorted by: $description"
    }

    private fun setGameInfo(game: Game?) {
        if (game == null) {
            return
        }
        if (game.recentPlayTime <= 0) {
            recentPlayTimeContainer.visibility = View.GONE
        } else {
            recentlyPlayedTextView.text = game.getRecentPlaytimeString()
        }

        totalPlayedTextView.text = game.getTotalPlayTimeString()

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
                                val darkSwatch = it.darkMutedSwatch
                                if (darkSwatch?.rgb != null) {
                                    setCollapsingToolbarColors(darkSwatch.rgb)
                                }

                                banner.setImageDrawable(resource)
                            }
                        }
                    }
                })

        collapsingToolbarLayout.title = game.name
        title = game.name

        setCollapsingToolbarColors(game.colorPrimaryDark)
    }

    private fun setAchievements(achievements: List<Achievement>) {
        achievementsAdapter.setAchievements(achievements)

        AchievementsGraphViewUtil.setAchievementsOverTime(achievementsOverTimeGraph, achievements)
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