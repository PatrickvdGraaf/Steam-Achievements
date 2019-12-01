package com.crepetete.steamachievements.ui.activity.main.fragment.library

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.SteamAchievementsApp
import com.crepetete.steamachievements.databinding.FragmentLibraryBinding
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.repository.resource.LiveResource
import com.crepetete.steamachievements.ui.activity.game.GameActivity
import com.crepetete.steamachievements.ui.common.adapter.GamesAdapter
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.vo.Game
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_library.*
import timber.log.Timber
import javax.inject.Inject

class LibraryFragment : Fragment(), Injectable, NavBarInteractionListener,
    GamesAdapter.GamesAdapterCallback {

    @Inject
    lateinit var viewModel: LibraryViewModel

    @Inject
    lateinit var imageLoader: ImageLoader

    lateinit var adapter: GamesAdapter

    lateinit var binding: FragmentLibraryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_library,
            container,
            false
        )

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = GamesAdapter(this)

        // Update the view with new data.
        viewModel.games.observe(viewLifecycleOwner, Observer { games ->
            if (games == null) {
                if (viewModel.gamesLoadingState.value == LiveResource.STATE_LOADING) {
                    // TODO add a better onboarding experience
                    showSnackBar(
                        "Loading your Library. This might take a while",
                        Snackbar.LENGTH_LONG
                    )
                }
            } else if (games.isNotEmpty()) {
                adapter.updateGames(games)

                pulsator.stop()
                pulsator.visibility = View.GONE

                if (games.flatMap { game -> game.achievements }.isEmpty()) {
                    showSnackBar(
                        "Fetching all achievements. This might take a while",
                        Snackbar.LENGTH_SHORT
                    )
                }
            } else {
                showSnackBar(
                    "We couldn't find any games in your library.",
                    Snackbar.LENGTH_LONG,
                    "Retry",
                    View.OnClickListener { viewModel.fetchGames() }
                )
            }
        })

        // Hide or show the pulsating loading view.
        viewModel.gamesLoadingState.observe(viewLifecycleOwner, Observer { state ->
            state?.let {
                when (state) {
                    LiveResource.STATE_LOADING -> {
                        pulsator.visibility = View.VISIBLE
                        pulsator.start()
                    }
                    LiveResource.STATE_SUCCESS -> {
                        pulsator.stop()
                        pulsator.visibility = View.GONE
                    }
                    LiveResource.STATE_FAILED -> {
                        pulsator.stop()
                    }
                }
            }
        })

        // Handle errors when updating the games list.
        viewModel.gamesLoadingError.observe(viewLifecycleOwner, Observer { error ->
            Timber.e("Error while loading Games: $error")

            when {
                error?.localizedMessage?.contains("Unable to resolve host") == true -> showSnackBar(
                    "Could not update games, are you connected to the internet?",
                    Snackbar.LENGTH_LONG,
                    "",
                    null
                )
                // If the  list is null or empty, we assume fetching has failed for the player.
                viewModel.games.value?.isEmpty() == true ->
                    showSnackBar(
                        "We couldn't find any games in your library.",
                        Snackbar.LENGTH_LONG,
                        "Retry",
                        View.OnClickListener { viewModel.fetchGames() }
                    )
                viewModel.games.value == null -> showSnackBar(
                    "Error while fetching games.",
                    Snackbar.LENGTH_LONG,
                    "Retry",
                    View.OnClickListener { viewModel.fetchGames() })
            }
        })

        initScrollFab()
        initRecyclerView()

        viewModel.fetchGames()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (activity!!.application as SteamAchievementsApp).appComponent.inject(this)
    }

    private fun initScrollFab() {
        fab.setOnClickListener {
            recycler_view_games.scrollToPosition(0)
        }
    }

    private fun initRecyclerView() {
        recycler_view_games.layoutManager = LinearLayoutManager(context)
        recycler_view_games.isNestedScrollingEnabled = false
        recycler_view_games.setHasFixedSize(true)
        recycler_view_games.setItemViewCacheSize(10)

        adapter.listener = this
        recycler_view_games.adapter = adapter
        recycler_view_games.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    private fun showSnackBar(
        message: String,
        duration: Int,
        actionMessage: String = "",
        clickListener: View.OnClickListener? = null
    ) {
        view?.let {
            Snackbar.make(it, message, duration)
                .setAction(actionMessage, clickListener)
                .show()
        }
    }

    /**
     * Invoked when a game item in the RecyclerView is clicked.
     *
     * Opens GameActivity and handles animation.
     */
    override fun onGameClicked(game: Game, imageView: ImageView, background: View, title: View) {
        startActivity(GameActivity.getInstance(requireContext(), game))
    }

    /**
     * Listener method for an updated search query. Updates the displayed games in the adapter.
     */
    override fun onSearchQueryUpdate(query: String) {
        adapter.updateQuery(query)
    }

    /**
     * Listener method for the sorting button of this fragments Activity, Updates the adapter with a
     * new sorting method.
     */
    override fun onSortingMethodChanged(sortingMethod: SortingType) {
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