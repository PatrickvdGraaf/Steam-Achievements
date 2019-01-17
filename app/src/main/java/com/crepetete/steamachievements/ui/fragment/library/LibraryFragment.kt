package com.crepetete.steamachievements.ui.fragment.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
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
import com.crepetete.steamachievements.ui.activity.game.GameActivity
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.common.adapter.games.GamesAdapter
import com.crepetete.steamachievements.ui.common.adapter.games.SortingType
import com.crepetete.steamachievements.util.autoCleared
import com.crepetete.steamachievements.vo.GameWithAchievements
import com.crepetete.steamachievements.vo.Status
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_library.*
import javax.inject.Inject

class LibraryFragment : Fragment(), Injectable,
    NavBarInteractionListener, GamesAdapter.OnGameBindListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    var adapter by autoCleared<GamesAdapter>()

    var binding by autoCleared<FragmentLibraryBinding>()

    private var dataBindingComponent = FragmentDataBindingComponent()

    private lateinit var viewModel: LibraryViewModel

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

        // Check if there is a userId property in the arguments.
        var userId: String
        arguments?.let {
            userId = it.getString(KEY_PLAYER_ID) ?: ""
            if (userId.isBlank()) {
                requireContext().startActivity(LoginActivity.getInstance(requireContext()))
                return
            }
        }

        // Provide ViewModel.
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LibraryViewModel::class.java)

        viewModel.gamesWithAchievement.observe(this, Observer { gameWithAchResponse ->
            when (gameWithAchResponse.status) {
                Status.SUCCESS -> {
                    progressBar.visibility = View.GONE
                    adapter.setGames(gameWithAchResponse.data)
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
            list_games.smoothScrollToPosition(0)
        }
    }

    private fun initRecyclerView() {
        list_games.layoutManager = LinearLayoutManager(context)

        adapter = GamesAdapter(
            appExecutors = appExecutors,
            dataBindingComponent = dataBindingComponent
        )

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
     * Updates the achievements for a specific game when it is shown in the RecyclerView.
     */
    override fun onGameBoundInAdapter(appId: String) {
        viewModel.updateAchievementsFor(appId)
    }

    /**
     * Invoked when a game item in the RecyclerView is clicked.
     *
     * Opens GameActivity and handles animation.
     */
    override fun onGameClicked(game: GameWithAchievements, imageView: ImageView, background: View, title: View) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            Pair.create(background, "background"),
            Pair.create(imageView as View, "banner"),
            Pair.create(title, "title")
        )

        startActivity(GameActivity.getInstance(requireContext(), game), options.toBundle())
    }

    /**
     * Invoked when the Adapter has created a primary rgb color for the games thumbnail.
     * Calls the ViewModel so it can update this property in the Database.
     */
    override fun onPrimaryGameColorCreated(appId: String, rgb: Int) {
        viewModel.updatePrimaryColorForGame(appId, rgb)
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

        fun getInstance(playerId: String): LibraryFragment {
            return LibraryFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_PLAYER_ID, playerId)
                }
            }
        }
    }

}