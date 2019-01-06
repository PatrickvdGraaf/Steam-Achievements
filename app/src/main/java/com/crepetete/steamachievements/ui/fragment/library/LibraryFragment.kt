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
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.binding.FragmentDataBindingComponent
import com.crepetete.steamachievements.databinding.FragmentLibraryBinding
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.ui.activity.game.startGameActivity
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.common.adapter.games.GamesAdapter
import com.crepetete.steamachievements.ui.common.adapter.games.SortingType
import com.crepetete.steamachievements.ui.common.helper.LoadingIndicator
import com.crepetete.steamachievements.util.autoCleared
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Game
import timber.log.Timber
import javax.inject.Inject

class LibraryFragment : Fragment(), Injectable,
    NavBarInteractionListener, GamesAdapter.OnGameBindListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    var adapter by autoCleared<GamesAdapter>()

    private var dataBindingComponent = FragmentDataBindingComponent(this)

    var binding by autoCleared<FragmentLibraryBinding>()

    private lateinit var viewModel: LibraryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_library, container,
            false, dataBindingComponent)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Provide ViewModel.
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(LibraryViewModel::class.java)

        // Check if there is a userId property in the arguments.
        var userId: String
        arguments?.let {
            userId = it.getString(KEY_PLAYER_ID) ?: ""
            if (userId.isBlank()) {
                // Go to Login page.
                context?.startActivity(LoginActivity.getInstance(requireContext()))
            }
            viewModel.setAppId(userId)
        }

        viewModel.gamesWithAchievement.observe(this, Observer { gameWithAchievements ->
            if (gameWithAchievements != null) {
                adapter.setGames(gameWithAchievements)
            }
        })

        initScrollFab()
        initRecyclerView()

        val gamesAdapter = GamesAdapter(
            appExecutors = appExecutors,
            dataBindingComponent = dataBindingComponent
        )

        gamesAdapter.listener = this

        binding.listGames.adapter = gamesAdapter
        adapter = gamesAdapter
    }

    /**
     * Updates the achievements for a specific game when it is shown in the RecyclerView.
     */
    override fun onGameBoundInAdapter(appId: String) {
        viewModel.updateAchievementsFor(appId, object : AchievementsRepository.AchievementsListener {
            override fun onAchievementsLoadedForGame(appId: String, achievements: List<Achievement>) {
                adapter.setAchievements(appId, achievements)
            }
        })
    }

    /**
     * Invoked when a game item in the RecyclerView is clicked.
     */
    override fun onGameClicked(appId: String, imageView: ImageView) {
        activity?.startGameActivity(appId, imageView)
    }

    /**
     * Invoked when the Adapter has created a primary rgb color for the games thumbnail.
     * Calls the ViewModel so it can update this property in the Database.
     */
    override fun onPrimaryGameColorCreated(appId: String, rgb: Int) {
        viewModel.updatePrimaryColorForGame(appId, rgb)
    }

    private fun initScrollFab() {
        binding.fab.setOnClickListener {
            binding.listGames.smoothScrollToPosition(0)
        }
    }

    private fun initRecyclerView() {
        binding.listGames.layoutManager = LinearLayoutManager(context)
        binding.listGames.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
     * Retrieved a new game from the presenter that needs to be added to the ListView.
     */
    fun addGame(game: Game) {
        //        gamesAdapter.addGame(game)
    }

    /**
     * Retrieved a list of emptyAchievements from the presenter, which need to be updated in the Adapter
     * for a specific game.
     *
     * @param appId ID of the game that own the emptyAchievements
     * @param achievements list of emptyAchievements for said game.
     */
    fun updateAchievementsForGame(appId: String, achievements: List<Achievement>) {
        //        gamesAdapter.updateAchievementsForGame(getAppId, emptyAchievements)
    }

    /**
     * Opens the GameActivity.
     *
     * @param appId ID for the Game which needs to be shown.
     * @param imageView Banner which displays the game's banner-image in the ListView, used to
     * animate to the next view.
     */
    fun showGameActivity(appId: String, imageView: ImageView) {
        activity?.startGameActivity(appId, imageView)
    }

    /**
     * Refreshes the list of games by first refreshing the games from the database, and then
     * automatically calls the API for an update.
     */
    fun refresh() {
        //        presenter.getGameIdsFromDb()
    }

    /**
     * A list of games have been received from the presenter and need to be updated in our adapter.
     */
    fun updateGames(games: List<Game>) {
        Timber.d("Updating games.")
        //        gamesAdapter.updateGames(games)
    }

    /**
     * Listener method for an updated search query. Updates the displayed games in the adapter.
     */
    override fun onSearchQueryUpdate(query: String) {
        // TODO fix Search
        //        gamesAdapter.updateSearchQuery(query)
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

        fun getInstance(playerId: String, loadingIndicator: LoadingIndicator): LibraryFragment {
            return LibraryFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_PLAYER_ID, playerId)
                }
                //                setLoaderIndicator(loadingIndicator)
            }
        }
    }

}