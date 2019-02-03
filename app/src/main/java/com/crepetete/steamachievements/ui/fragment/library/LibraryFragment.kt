package com.crepetete.steamachievements.ui.fragment.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.binding.FragmentDataBindingComponent
import com.crepetete.steamachievements.databinding.FragmentLibraryBinding
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.ui.activity.game.GameActivity
import com.crepetete.steamachievements.ui.common.adapter.GamesAdapter
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.crepetete.steamachievements.vo.Status
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_library.*
import javax.inject.Inject

class LibraryFragment : Fragment(), Injectable, NavBarInteractionListener, GamesAdapter.OnGameBindListener,
    AchievementsRepository.PrivateProfileMessageListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LibraryViewModel

    private var hasShownPrivateProfileMessage = false

    var adapter = GamesAdapter()

    lateinit var binding: FragmentLibraryBinding

    private var dataBindingComponent = FragmentDataBindingComponent()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_library,
            container,
            false,
            dataBindingComponent
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Provide ViewModel.
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewModel::class.java)

        viewModel.gamesWithAchievement.observe(this, Observer { gameWithAchResponse ->
            when (gameWithAchResponse.status) {
                Status.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    adapter.updateGames(gameWithAchResponse.data)
                    gameWithAchResponse.data?.map { game -> game.getAppId() }?.forEach { id ->
                        viewModel.updateAchievements(id, this).observe(this, Observer {
                            // Just observe, otherwise the NetworkBoundResource won't fire and achievements wont be fetched.
                        })
                    }
                }
                Status.ERROR -> {
                    progressBar.visibility = View.GONE
                    Snackbar.make(coordinator, "Error while updating Games.", Snackbar.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    progressBar.visibility = View.VISIBLE
                }
            }
        })

        initScrollFab()
        initRecyclerView()
    }

    private fun initScrollFab() {
        fab.setOnClickListener {
            list_games.scrollToPosition(0)
        }
    }

    private fun initRecyclerView() {
        list_games.layoutManager = LinearLayoutManager(context)
        list_games.setHasFixedSize(true)

        adapter = GamesAdapter()

        adapter.listener = this
        list_games.adapter = adapter
        list_games.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) {
                    binding.fab.hide()
                } else {
                    binding.fab.show()
                }
            }
        })
    }

    // TODO make a nicer
    override fun onPrivateModelMessage() {
        if (!hasShownPrivateProfileMessage) {
            hasShownPrivateProfileMessage = true
            Snackbar.make(list_games, "Could not get personal stats. Your profile is not public.", Snackbar.LENGTH_SHORT).show()
        }
    }

    /**
     * Updates the achievements for a specific game when it is shown in the RecyclerView.
     */
    override fun onGameBoundInAdapter(appId: String) {
        //        viewModel.updateAchievementsFor(appId)
    }

    /**
     * Invoked when a game item in the RecyclerView is clicked.
     *
     * Opens GameActivity and handles animation.
     */
    override fun onGameClicked(game: GameWithAchievements, imageView: ImageView, background: View, title: View) {
        //        startActivity(
        //            GameActivity.getInstance(requireContext(), game),
        //            ActivityOptions.makeSceneTransitionAnimation(
        //                requireActivity(),
        //                Pair.create(background, "background"),
        //                Pair.create(imageView as View, "banner"),
        //                Pair.create(title, "title")
        //            ).toBundle()
        //        )

        val intent = GameActivity.getInstance(requireContext(), game)
        startActivity(intent)
        //        activity?.overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_from_top)
    }

    /**
     * Invoked when the Adapter has created a primary rgb color for the games thumbnail.
     * Calls the ViewModel so it can update this property in the Database.
     */
    override fun onPrimaryGameColorCreated(game: GameWithAchievements, rgb: Int) {
        viewModel.updatePrimaryColorForGame(game, rgb)
    }

    /**
     * Listener method for an updated search query. Updates the displayed games in the adapter.
     */
    override fun onSearchQueryUpdate(query: String) {
        adapter.setQuery(query)
    }

    /**
     * Listener method for the sorting button of this fragments Activity, Updates the adapter with a
     * new sorting method.
     */
    override fun onSortingMethodChanged(sortingMethod: SortingType) {
        // TODO fix sorting
        //        viewModel.rearrangeGames(sortingMethod)
    }

    companion object {
        const val TAG = "LIBRARY_FRAGMENT"
        private const val KEY_PLAYER_ID = "KEY_PLAYER_ID"

        fun getInstance(playerId: String): LibraryFragment {
            return LibraryFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_PLAYER_ID, playerId)
                }
            }
        }
    }

}