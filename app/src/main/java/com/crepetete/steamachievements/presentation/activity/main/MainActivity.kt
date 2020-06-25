package com.crepetete.steamachievements.presentation.activity.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.presentation.activity.BaseActivity
import com.crepetete.steamachievements.presentation.common.enums.SortingType
import com.crepetete.steamachievements.presentation.common.helper.LoadingIndicator
import com.crepetete.steamachievements.presentation.fragment.BaseFragment
import com.crepetete.steamachievements.presentation.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.presentation.fragment.game.GameFragment
import com.crepetete.steamachievements.presentation.fragment.library.LibraryFragment
import com.crepetete.steamachievements.presentation.fragment.library.NavBarInteractionListener
import com.crepetete.steamachievements.presentation.fragment.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

/**
 *
 */
class MainActivity : BaseActivity(), LoadingIndicator,
    BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        fun getInstance(context: Context, userId: String) = Intent(
            context,
            MainActivity::class.java
        ).apply { putExtra(INTENT_USER_ID, userId) }
    }

    @IdRes
    private var selectedNavItem = R.id.menu_library

    private var currentTag = ""

    private var navBarListener: NavBarInteractionListener? = null

    @IdRes
    private val containerId: Int = R.id.container
    private val fragmentManager: FragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        setTranslucentStatusBar()

        /* Set a reference to the view responsible for showing a loader indicator. */
        //        loadingIndicator = findViewById(R.id.pulsator)

        handleIntent(intent)

//        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
//        navigation.selectedItemId = R.id.menu_library
//        navigation.setOnNavigationItemSelectedListener(this)
//
//        navigation.selectedItemId = R.id.menu_library

        showLibraryFragment()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            handleIntent(intent)
        }
    }

    override fun setTitle(title: CharSequence?) {
        collapsingToolbar.title = title
    }

    override fun onBackPressed() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount != 0) {
            fragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    fun resetToolbar(title: String) {
        appbar.setExpanded(false)
        collapsingToolbar.title = title

        collapsingToolbar.setContentScrimColor(getColor(R.color.colorPrimary))
        updateNavigationBarColor(getColor(R.color.colorPrimary))
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.let { query ->
                navBarListener?.onSearchQueryUpdate(query)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_main, menu)
//
//        val searchItem = menu?.findItem(R.id.action_search)
//        val searchView = searchItem?.actionView as SearchView
//        // Configure the search info and add any event listeners...
//
//        // Associate searchable configuration with the SearchView
//        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextChange(newText: String): Boolean {
//                navBarListener?.onSearchQueryUpdate(newText)
//                return true
//            }
//
//            override fun onQueryTextSubmit(query: String): Boolean {
//                navBarListener?.onSearchQueryUpdate(query)
//                searchView.clearFocus()
//                return true
//            }
//        })

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
                showLibraryFragment()
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

    private fun performTransAction(transaction: FragmentTransaction, fragment: BaseFragment) {
        currentTag = fragment.getFragmentName()
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        transaction.replace(containerId, fragment, currentTag)
            .addToBackStack(null)
            .commit()
    }

    private fun showLibraryFragment() {
        appbar.setExpanded(false)
        collapsingToolbar.title = "Library"

        updateNavigationBarColor(getColor(R.color.colorPrimary))

        appbar.setLiftable(true)
        appbar.setExpanded(true)

        val transaction = fragmentManager.beginTransaction()
        val fragment = LibraryFragment.getInstance(userId)
        performTransAction(transaction, fragment)
    }

    fun showGameFragment(game: Game) {
        collapsingToolbar.title = game.getName()

        val transaction = fragmentManager.beginTransaction()
        val fragment = GameFragment.getInstance(game)
        performTransAction(transaction, fragment)

        Glide.with(this)
            .load(game.getBannerUrl())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(banner)

        appbar.setLiftable(true)
        appbar.setExpanded(true)
        updateNavigationBarColor(game.getPrimaryColor())
    }

    fun showAllAchievementsFragment() {
        collapsingToolbar.title = "Achievements"

        val transaction = fragmentManager.beginTransaction()
        val fragment = AchievementsFragment.getInstance(userId)
        performTransAction(transaction, fragment)

        appbar.setLiftable(false)
        appbar.setExpanded(false)
        updateNavigationBarColor(getColor(R.color.colorPrimary))
    }

    private fun updateNavigationBarColor(primaryColor: Int) {
        collapsingToolbar.setContentScrimColor(primaryColor)
        collapsingToolbar.setStatusBarScrimColor(primaryColor)
    }
}
