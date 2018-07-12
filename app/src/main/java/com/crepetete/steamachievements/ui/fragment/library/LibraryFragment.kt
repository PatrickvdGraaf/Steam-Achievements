package com.crepetete.steamachievements.ui.fragment.library

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseFragment
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.ui.activity.helper.LoadingIndicator
import com.crepetete.steamachievements.ui.fragment.library.adapter.GamesAdapter
import timber.log.Timber

class LibraryFragment : BaseFragment<LibraryPresenter>(), LibraryView, NavbarInteractionListener {
    private val gamesAdapter by lazy { GamesAdapter(this, presenter) }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initially gets games from DB.
        presenter.getGamesFromDatabase()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        val listView = view.findViewById<RecyclerView>(R.id.list_games)
        listView.adapter = gamesAdapter
        listView.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun updateGames(games: List<Game>) {
        Timber.d("Updating games.")
        gamesAdapter.updateGames(games)
    }

    override fun onSearchQueryUpdate(query: String) {
        gamesAdapter.updateSearchQuery(query)
    }

    override fun onSortingMethodChanged(sortingMethod: Int) {
        gamesAdapter.sort(sortingMethod)
    }

    /**
     * Instantiates the presenter the Activity is based on.
     */
    override fun instantiatePresenter(): LibraryPresenter {
        var userId: String
        arguments?.let {
            userId = it.getString(KEY_PLAYER_ID)
            if (userId.isBlank()) {
                // TODO go to login page.
            }
        }

        return LibraryPresenter(this)
    }
}