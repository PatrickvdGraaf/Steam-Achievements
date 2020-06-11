package com.crepetete.steamachievements.presentation.activity.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.presentation.activity.BaseActivity
import com.crepetete.steamachievements.presentation.common.enums.SortingType
import com.crepetete.steamachievements.presentation.common.helper.LoadingIndicator
import com.crepetete.steamachievements.presentation.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.presentation.fragment.library.LibraryFragment
import com.crepetete.steamachievements.presentation.fragment.library.NavBarInteractionListener
import com.crepetete.steamachievements.presentation.fragment.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 *
 */
class MainActivity : BaseActivity(), LoadingIndicator, BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        fun getInstance(context: Context, userId: String) = Intent(
            context,
            MainActivity::class.java
        ).apply { putExtra(INTENT_USER_ID, userId) }
    }

    @IdRes
    private var selectedNavItem = R.id.menu_library

    private var currentTag = LibraryFragment.TAG

    private var navBarListener: NavBarInteractionListener? = null

    @IdRes
    private val containerId: Int = R.id.fragment_container
    private val fragmentManager: FragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        /* Set a reference to the view responsible for showing a loader indicator. */
        //        loadingIndicator = findViewById(R.id.pulsator)

        handleIntent(intent)

        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.selectedItemId = R.id.menu_library
        navigation.setOnNavigationItemSelectedListener(this)

        navigation.selectedItemId = R.id.menu_library
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
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
            intent.getStringExtra(SearchManager.QUERY)?.let { query ->
                navBarListener?.onSearchQueryUpdate(query)
            }
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
                return true
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
                    fragment = AchievementsFragment.getInstance(userId)
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
        //        loadingIndicator.start()
        //        loadingIndicator.visibility = View.VISIBLE
    }

    /**
     * Hides the loading indicator of the view
     */
    override fun hideLoading() {
        //        loadingIndicator.stop()
        //        loadingIndicator.visibility = View.GONE
    }
}
