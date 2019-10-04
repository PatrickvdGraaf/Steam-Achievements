package com.crepetete.steamachievements.ui.activity.main.fragment.library

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
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.binding.FragmentDataBindingComponent
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

class LibraryFragment : Fragment(), Injectable, NavBarInteractionListener, GamesAdapter.OnGameClickListener {

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


        with(viewModel) {
            viewModel.data.observe(this@LibraryFragment, Observer {
                setGamesData(it)
            })

            gamesLoadingState.observe(this@LibraryFragment, Observer { state ->
                state?.let {
                    when (state) {
                        LiveResource.STATE_LOADING -> {
                            pulsator.visibility = View.VISIBLE
                            pulsator.start()
                        }
                        LiveResource.STATE_FAILED, LiveResource.STATE_SUCCESS -> {
                            pulsator.visibility = View.GONE
                            pulsator.stop()
                        }
                    }
                }
            })

            gamesLoadingError.observe(this@LibraryFragment, Observer { error ->
                Timber.e("Error while loading Games: $error")

                if (error?.localizedMessage?.contains("Unable to resolve host") == true) {
                    Snackbar.make(
                        coordinator,
                        "Could not update games, are you connected to the internet?",
                        Snackbar.LENGTH_LONG).show()
                }
            })

            fetchGames()
        }

        initScrollFab()
        initRecyclerView()
    }

    private fun setGamesData(games: List<Game>) {
        if (games.isEmpty()) {
            pulsator.visibility = View.VISIBLE

            Snackbar.make(coordinator, "We couldn't find any games in your library.", Snackbar.LENGTH_LONG).setAction("Retry") {
                viewModel.refresh()
            }.show()
        } else {
            pulsator.visibility = View.GONE
        }

        adapter.updateGames(games)
        viewModel.updateAchievementsForGames(games)
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

    /**
     * Invoked when a game item in the RecyclerView is clicked.
     *
     * Opens GameActivity and handles animation.
     */
    override fun onGameClicked(game: Game, imageView: ImageView, background: View, title: View, palette: Palette?) {
        startActivity(GameActivity.getInstance(requireContext(), game, palette))
    }

    /**
     * Invoked when the Adapter has created a primary rgb color for the games thumbnail.
     * Calls the ViewModel so it can update this property in the Database.
     */
    override fun onPrimaryGameColorCreated(game: Game, rgb: Int) {
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