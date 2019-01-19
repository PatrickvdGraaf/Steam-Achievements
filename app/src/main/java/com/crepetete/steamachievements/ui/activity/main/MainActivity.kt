package com.crepetete.steamachievements.ui.activity.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.common.adapter.games.SortingType
import com.crepetete.steamachievements.ui.common.helper.LoadingIndicator
import com.crepetete.steamachievements.ui.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.ui.fragment.library.LibraryFragment
import com.crepetete.steamachievements.ui.fragment.library.NavBarInteractionListener
import com.crepetete.steamachievements.ui.fragment.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class MainActivity : AppCompatActivity(), LoadingIndicator,
    BottomNavigationView.OnNavigationItemSelectedListener, HasSupportFragmentInjector {
    companion object {
        private const val INTENT_USER_ID = "user_id"

        fun getInstance(context: Context, id: String): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(INTENT_USER_ID, id)
            }
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @IdRes
    private var selectedNavItem = R.id.menu_library

    private var currentTag = LibraryFragment.TAG

    private var navBarListener: NavBarInteractionListener? = null

    @IdRes
    private val containerId: Int = R.id.fragment_container
    private val fragmentManager: FragmentManager = supportFragmentManager

    private lateinit var loadingIndicator: ProgressBar

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val restoredId = savedInstanceState?.getString(INTENT_USER_ID)
        userId = if (restoredId.isNullOrBlank()) {
            intent.getStringExtra(INTENT_USER_ID)
        } else {
            restoredId
        }

        if (userId.isBlank()) {
            openLoginActivity()
            return
        }

        loadingIndicator = findViewById(R.id.progressBar)
        handleIntent(intent)

        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.selectedItemId = R.id.menu_library
        navigation.setOnNavigationItemSelectedListener(this)

        navigation.selectedItemId = R.id.menu_library

//        val fragment: LibraryFragment = LibraryFragment.getInstance(userId)
//        fragmentManager.beginTransaction()
//            .replace(containerId, fragment, LibraryFragment.TAG)
//            .addToBackStack(null)
//            .commit()
//        navBarListener = fragment
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
            navBarListener?.onSearchQueryUpdate(query)
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
                navBarListener?.onSearchQueryUpdate(newText)
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                navBarListener?.onSearchQueryUpdate(query)
                searchView.clearFocus()
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menuSortCompletion -> {
            navBarListener?.onSortingMethodChanged(SortingType.COMPLETION)
            true
        }
        R.id.menuSortName -> {
            navBarListener?.onSortingMethodChanged(SortingType.NAME)
            true
        }
        R.id.menuSortPlaytime -> {
            navBarListener?.onSortingMethodChanged(SortingType.PLAYTIME)
            true
        }
        R.id.action_refresh -> {
            // TODO fix refresh
//            presenter.onRefreshClicked()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        val transaction = fragmentManager.beginTransaction()

        selectedNavItem = item.itemId
        when (selectedNavItem) {
            R.id.menu_profile -> {
                currentTag = ProfileFragment.TAG
                fragment = fragmentManager.findFragmentByTag(currentTag)
                if (fragment == null) {
                    fragment = ProfileFragment.getInstance(userId)
                }
            }
            R.id.menu_library -> {
                currentTag = LibraryFragment.TAG
                fragment = fragmentManager.findFragmentByTag(currentTag)
                if (fragment == null) {
                    fragment = LibraryFragment.getInstance(userId)
                }
                navBarListener = fragment as LibraryFragment
            }
            R.id.menu_achievements -> {
                currentTag = AchievementsFragment.TAG
                fragment = fragmentManager.findFragmentByTag(currentTag)
                if (fragment == null) {
                    fragment = AchievementsFragment.getInstance(userId, this)
                }
            }
        }

        navBarListener = if (fragment is NavBarInteractionListener) {
            fragment
        } else {
            null
        }

        updateTitle()

        if (fragment != null) {
            transaction.replace(containerId, fragment, currentTag)
                .addToBackStack(null)
                .commit()
        }
        return true
    }

    // TODO let Fragments set Page titles.
    private fun updateTitle() {
        //        when (selectedNavItem) {
        //            R.id.menu_profile -> setTitle("Profile")
        //            R.id.menu_achievements -> setTitle("${presenter.currentPlayer.value?.persona
        //                    ?: "players"}'s Achievements")
        //            else -> setTitle("${presenter.currentPlayer.value?.persona ?: "players"}'s Library")
        //        }
    }

    /**
     * Displays the loading indicator of the view
     */
    override fun showLoading() {
        //        loadingIndicator.visibility = View.VISIBLE
    }

    /**
     * Hides the loading indicator of the view
     */
    override fun hideLoading() {
        //        loadingIndicator.visibility = View.GONE
    }

    private fun openLoginActivity() {
        startActivity(LoginActivity.getInstance(this))
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector
}
