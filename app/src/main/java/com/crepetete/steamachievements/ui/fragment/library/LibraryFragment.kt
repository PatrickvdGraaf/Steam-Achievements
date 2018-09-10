package com.crepetete.steamachievements.ui.fragment.library

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.RefreshableFragment
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.ui.activity.game.startGameActivity
import com.crepetete.steamachievements.ui.activity.helper.LoadingIndicator
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.view.game.adapter.GamesAdapter
import timber.log.Timber
import javax.inject.Inject


class LibraryFragment : RefreshableFragment<LibraryPresenter>(), LibraryView,
        NavbarInteractionListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val gamesAdapter by lazy { GamesAdapter(this, presenter) }

    private lateinit var viewModel: LibraryViewModel
    private lateinit var scrollToTopButton: FloatingActionButton

    companion object {
        const val TAG = "LIBRARY_FRAGMENT"
        private const val KEY_PLAYER_ID = "KEY_PLAYER_ID"

        fun getInstance(playerId: String, loadingIndicator: LoadingIndicator): LibraryFragment {
            return LibraryFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_PLAYER_ID, playerId)
                }
                setLoaderIndicator(loadingIndicator)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        // Initialise ListView and Adapter.
        val listView = view.findViewById<RecyclerView>(R.id.list_games)
        listView.adapter = gamesAdapter
        listView.layoutManager = LinearLayoutManager(context)


        // Set up FAB to scroll up.
        scrollToTopButton = view.findViewById(R.id.fab)
        scrollToTopButton.setOnClickListener {
            listView.smoothScrollToPosition(0)
        }

        // Hides/ shows the FAB if the user scrolls.
        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) {
                    scrollToTopButton.hide()
                } else {
                    scrollToTopButton.show()
                }
            }
        })

        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(LibraryViewModel::class.java)

        var userId: String
        arguments?.let {
            userId = it.getString(KEY_PLAYER_ID)
            if (userId.isBlank()) {
                context.startActivity(LoginActivity.getInstance(context))
            }
            viewModel.setAppId(userId)
        }

        viewModel.games.observe(this, Observer { gamesResource ->
            val games = gamesResource?.data
            if (games != null) {
                gamesAdapter.updateGames(games)
            }
        })

        return view
    }

    /**
     * Retrieved a new game from the presenter that needs to be added to the ListView.
     */
    override fun addGame(game: Game) {
        gamesAdapter.addGame(game)
    }

    /**
     * Retrieved a list of achievements from the presenter, which need to be updated in the Adapter
     * for a specific game.
     *
     * @param appId ID of the game that own the achievements
     * @param achievements list of achievements for said game.
     */
    override fun updateAchievementsForGame(appId: String, achievements: List<Achievement>) {
        gamesAdapter.updateAchievementsForGame(appId, achievements)
    }

    /**
     * Opens the GameActivity.
     *
     * @param appId ID for the Game which needs to be shown.
     * @param imageView Banner which displays the game's banner-image in the ListView, used to
     * animate to the next view.
     */
    override fun showGameActivity(appId: String, imageView: ImageView) {
        activity?.startGameActivity(appId, imageView)
    }

    /**
     * Refreshes the list of games by first refreshing the games from the database, and then
     * automatically calls the API for an update.
     */
    override fun refresh() {
        presenter.getGameIdsFromDb()
    }

    /**
     * A list of games have been received from the presenter and need to be updated in our adapter.
     */
    override fun updateGames(games: List<Game>) {
        Timber.d("Updating games.")
        gamesAdapter.updateGames(games)
    }

    /**
     * Listener method for an updated search query. Updates the displayed games in the adapter.
     */
    override fun onSearchQueryUpdate(query: String) {
        gamesAdapter.updateSearchQuery(query)
    }

    /**
     * Listener method for the sorting button of this fragments Activity, Updates the adapter with a
     * new sorting method.
     */
    override fun onSortingMethodChanged(sortingMethod: Int) {
        gamesAdapter.sort(sortingMethod)
    }
}