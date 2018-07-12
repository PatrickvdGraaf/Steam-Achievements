package com.crepetete.steamachievements.ui.activity.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseActivity
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.ui.activity.helper.LoadingIndicator
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.fragment.library.adapter.GamesAdapter


class MainActivity : BaseActivity<MainPresenter>(), MainView, LoadingIndicator {
    companion object {
        private const val INTENT_USER_ID = "user_id"

        fun getInstance(context: Context, id: String): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(INTENT_USER_ID, id)
            }
        }
    }

    private lateinit var loadingIndicator: ProgressBar

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val restoredId = savedInstanceState?.getString(INTENT_USER_ID)
        userId = if (restoredId.isNullOrBlank()) {
            intent.getStringExtra(INTENT_USER_ID)
        } else {
            restoredId!!
        }

        loadingIndicator = findViewById(R.id.progressBar)
        handleIntent(intent)

        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.selectedItemId = R.id.menu_library
        navigation.setOnNavigationItemSelectedListener(presenter)

        presenter.onViewCreated()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            putString(INTENT_USER_ID, userId)
        }
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        if (intent != null) {
            handleIntent(intent)
        }
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount != 0) {
            fragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            presenter.onSearchQueryChanged(query)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        // Configure the search info and add any event listeners...

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                presenter.onSearchQueryChanged(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                searchView.clearFocus()
                presenter.onSearchQueryChanged(query)
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menuSortCompletion -> {
            presenter.onSortingMerhodChanged(GamesAdapter.COMPLETION)
            true
        }
        R.id.menuSortName -> {
            presenter.onSortingMerhodChanged(GamesAdapter.NAME)
            true
        }
        R.id.menuSortPlaytime -> {
            presenter.onSortingMerhodChanged(GamesAdapter.PLAYTIME)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun showPlayerDetails(persona: String) {
        title = "$persona's Games"
    }

    override fun setTitle(title: String) {
        this.title = title
    }

    override fun showAchievements(achievements: List<Achievement>, appId: String) {
//        gamesAdapter.addAchievementsForGame(achievements, appId)
    }

    override fun showGames(games: List<Game>) {
//        gamesAdapter.updateGames(games)
    }

    /**
     * Displays the loading indicator of the view
     */
    override fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
    }

    /**
     * Hides the loading indicator of the view
     */
    override fun hideLoading() {
        loadingIndicator.visibility = View.GONE
    }

    override fun onDestroy() {
        presenter.onViewDestroyed()
        super.onDestroy()
    }

    /**
     * Instantiates the presenter the Activity is based on.
     */
    override fun instantiatePresenter(): MainPresenter {
        val intentUserId = intent.getStringExtra(INTENT_USER_ID)
        if (intentUserId.isBlank()) {
            openLoginActivity()
        }
        return MainPresenter(this, R.id.fragment_container, supportFragmentManager,
                intentUserId)
    }

    override fun openLoginActivity() {
        startActivity(LoginActivity.getInstance(this))
    }
}
