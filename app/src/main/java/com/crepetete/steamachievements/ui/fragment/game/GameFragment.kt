package com.crepetete.steamachievements.ui.fragment.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.binding.FragmentDataBindingComponent
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.activity.achievements.pager.TransparentPagerActivity
import com.crepetete.steamachievements.ui.activity.game.GameViewModel
import com.crepetete.steamachievements.ui.common.adapter.HorizontalAchievementsAdapter
import com.crepetete.steamachievements.ui.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.ui.common.graph.point.OnGraphDateTappedListener
import com.crepetete.steamachievements.ui.fragment.library.LibraryFragment
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.GameData
import com.crepetete.steamachievements.vo.GameWithAchievements
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class GameFragment : Fragment(), Injectable, HorizontalAchievementsAdapter.OnAchievementClickListener, OnGraphDateTappedListener {

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

        fun getInstance(game: GameWithAchievements, palette: Palette?): LibraryFragment {
            return LibraryFragment().apply {
                arguments = Bundle(1).apply {
                    putParcelable(INTENT_GAME, game)
                    putInt(INTENT_PALETTE_DARK_MUTED, palette?.darkMutedSwatch?.rgb ?: -1)
                    putInt(INTENT_PALETTE_DARK_VIBRANT, palette?.darkVibrantSwatch?.rgb ?: -1)
                    putInt(INTENT_PALETTE_LIGHT_MUTED, palette?.lightMutedSwatch?.rgb ?: -1)
                    putInt(INTENT_PALETTE_LIGHT_VIBRANT, palette?.lightVibrantSwatch?.rgb ?: -1)
                    putInt(INTENT_PALETTE_MUTED, palette?.mutedSwatch?.rgb ?: -1)
                    putInt(INTENT_PALETTE_VIBRANT, palette?.vibrantSwatch?.rgb ?: -1)
                    putInt(INTENT_PALETTE_DOMINANT, palette?.dominantSwatch?.rgb ?: -1)
                }
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: GameViewModel

    lateinit var binding: com.crepetete.steamachievements.databinding.FragmentGameBinding

    private val achievementsAdapter by lazy { HorizontalAchievementsAdapter(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_game,
            container,
            false,
            FragmentDataBindingComponent()
        )

        // Set status bar tint.
        setTranslucentStatusBar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setColorsWithIntent(arguments)

        val id = arguments?.getString(INTENT_GAME_ID) ?: "-1"
        if (id != "-1") {
            viewModel.setAppId(id)
        } else {
            arguments?.getParcelable<GameWithAchievements>(INTENT_GAME)?.let { game ->
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
        startActivity(TransparentPagerActivity.getInstance(requireContext(), index, sortedList))
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
            .load(data.getImageUrl())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(banner)

        // Prepare Achievements RecyclerView.
        recyclerViewAchievements.layoutManager = LinearLayoutManager(
            requireContext(),
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

    private fun setTranslucentStatusBar(color: Int = ContextCompat.getColor(requireContext(), R.color.statusbar_translucent)) {
        requireActivity().window.statusBarColor = color
    }

    private fun setColorsWithIntent(arguments: Bundle?) {
        val darkMuted = arguments?.getInt(INTENT_PALETTE_DARK_VIBRANT, -1) ?: -1
        val darkVibrant = arguments?.getInt(INTENT_PALETTE_DARK_MUTED, -1) ?: -1
        val lightMuted = arguments?.getInt(INTENT_PALETTE_LIGHT_MUTED, -1) ?: -1
        val lightVibrant = arguments?.getInt(INTENT_PALETTE_LIGHT_VIBRANT, -1) ?: -1
        val muted = arguments?.getInt(INTENT_PALETTE_MUTED, -1) ?: -1
        val vibrant = arguments?.getInt(INTENT_PALETTE_VIBRANT, -1) ?: -1
        val dominant = arguments?.getInt(INTENT_PALETTE_DOMINANT, -1) ?: -1

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

    override fun onGraphDateTapped(date: Date) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
