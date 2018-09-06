package com.crepetete.steamachievements.ui.fragment.library

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


class LibraryFragment : RefreshableFragment<LibraryPresenter>(), LibraryView, NavbarInteractionListener {
    private val gamesAdapter by lazy { GamesAdapter(this, presenter) }
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

        val listView = view.findViewById<RecyclerView>(R.id.list_games)
        listView.adapter = gamesAdapter
        listView.layoutManager = LinearLayoutManager(context)

        scrollToTopButton = view.findViewById(R.id.fab)
        scrollToTopButton.setOnClickListener {
            listView.smoothScrollToPosition(0)
        }

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

        return view
    }

    override fun addGame(game: Game) {
        gamesAdapter.addGame(game)
    }

    override fun updateAchievementsForGame(appId: String, achievements: List<Achievement>) {
        gamesAdapter.updateAchievementsForGame(appId, achievements)
    }

    override fun showGameActivity(appId: String, imageView: ImageView) {
        activity?.startGameActivity(appId, imageView)
    }

    override fun refresh() {
        showLoading()
        presenter.getGamesFromApi()
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
                context.startActivity(LoginActivity.getInstance(context))
            }
        }

        return LibraryPresenter(this)
    }
}