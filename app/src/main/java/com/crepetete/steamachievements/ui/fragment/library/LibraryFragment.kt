package com.crepetete.steamachievements.ui.fragment.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.RefreshableFragment
import com.crepetete.steamachievements.binding.FragmentDataBindingComponent
import com.crepetete.steamachievements.data.repository.game.GameRepository
import com.crepetete.steamachievements.databinding.FragmentLibraryBinding
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.ui.activity.game.startGameActivity
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.common.adapter.games.GamesAdapter
import com.crepetete.steamachievements.ui.common.adapter.games.SortingType
import com.crepetete.steamachievements.ui.common.helper.LoadingIndicator
import com.crepetete.steamachievements.utils.autoCleared
import timber.log.Timber
import javax.inject.Inject


class LibraryFragment : RefreshableFragment<LibraryPresenter>(), LibraryView,
        NavBarInteractionListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var gamesRepository: GameRepository

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
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(LibraryViewModel::class.java)

        var userId: String
        arguments?.let {
            userId = it.getString(KEY_PLAYER_ID) ?: ""
            if (userId.isBlank()) {
                context.startActivity(LoginActivity.getInstance(context))
            }
            viewModel.setAppId(userId)
        }

        viewModel.finalData.observe(this, Observer { games ->
            if (games != null) {
                adapter.submitList(games)
            }
        })

        viewModel.games.observe(this, Observer {
            if (it?.data != null) {
                it.data.mapNotNull { it1 -> it1.game?.appId }.forEach { id ->
                    viewModel.updateAchievementsFor(id)
                }
            }
        })

        viewModel.achievements.observe(this, Observer { allAchievements ->
            if (allAchievements != null) {
                viewModel.games.value?.data?.let { data ->
                    data.asSequence().map { it.game?.appId }.filter { it != null }.toList()
                            .forEach { appId ->
                                val achievements = allAchievements.filter { it.appId == appId }
                                if (achievements.isNotEmpty()) {
                                    viewModel.updateAchievedStats(appId!!, achievements)
                                    viewModel.updateGlobalStats(appId, achievements)
                                }
                            }
                }
            }
        })

        initScrollFab()
        initRecyclerView()

        val gamesAdapter = GamesAdapter(
                dataBindingComponent = dataBindingComponent,
                appExecutors = appExecutors,
                gameRepository = gamesRepository,
                gameClickCallback = { game, imageView ->
                    activity?.startGameActivity(game.appId, imageView)
                }
        )
        binding.listGames.adapter = gamesAdapter
        adapter = gamesAdapter
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
    override fun addGame(game: Game) {
//        gamesAdapter.addGame(game)
    }

    /**
     * Retrieved a list of emptyAchievements from the presenter, which need to be updated in the Adapter
     * for a specific game.
     *
     * @param appId ID of the game that own the emptyAchievements
     * @param achievements list of emptyAchievements for said game.
     */
    override fun updateAchievementsForGame(appId: String, achievements: List<Achievement>) {
//        gamesAdapter.updateAchievementsForGame(appId, emptyAchievements)
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
        viewModel.rearrangeGames(sortingMethod)
    }

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

}