package com.crepetete.steamachievements.presentation.fragment.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.presentation.activity.game.GameActivity
import com.crepetete.steamachievements.presentation.common.adapter.GamesAdapter
import com.crepetete.steamachievements.presentation.common.enums.SortingType
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_library.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class LibraryFragment : Fragment(), NavBarInteractionListener, GamesAdapter.GamesAdapterCallback {

    private val viewModel: LibraryViewModel by viewModel()

    private val adapter = GamesAdapter(this).apply {
        setHasStableIds(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Update the view with new data.
        viewModel.gamesLiveData.observe(viewLifecycleOwner, Observer { games ->
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
                        Snackbar.LENGTH_LONG
                    )
                }
            } else {
                showSnackBar(
                    "We couldn't find any games in your library.",
                    Snackbar.LENGTH_LONG,
                    "Retry",
                    View.OnClickListener { viewModel.updateGameData() }
                )
            }
        })

        // Hide or show the pulsating loading view.
        viewModel.gamesLoadingState.observe(viewLifecycleOwner, Observer { state ->
            if (viewModel.gamesLiveData.value.isNullOrEmpty()) {
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
            } else {
                pulsator.stop()
                pulsator.visibility = View.GONE
            }
        })

        // Handle errors when updating the games list.
        viewModel.gamesLoadingError.observe(viewLifecycleOwner, Observer { error ->
            Timber.e("Error while loading Games: $error")
            if (error is IllegalArgumentException) {
                Timber.e("${error.localizedMessage} + {${error.message}")
            }

            when {
                error?.localizedMessage?.contains("Unable to resolve host") == true -> showSnackBar(
                    "Could not update games, are you connected to the internet?",
                    Snackbar.LENGTH_LONG,
                    "",
                    null
                )
            }
        })

        initScrollFab()
        initRecyclerView()

        viewModel.updateGameData()
    }

    private fun initScrollFab() {
        fab.setOnClickListener {
            recycler_view_games.scrollToPosition(0)
        }
    }

    private fun initRecyclerView() {
        recycler_view_games.layoutManager = LinearLayoutManager(context)
        recycler_view_games.isNestedScrollingEnabled = false
        recycler_view_games.setHasFixedSize(false)
        recycler_view_games.setItemViewCacheSize(10)

        adapter.listener = this
        recycler_view_games.adapter = adapter
        recycler_view_games.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) {
                    fab.hide()
                } else {
                    fab.show()
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