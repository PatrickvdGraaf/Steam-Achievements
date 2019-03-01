package com.crepetete.steamachievements.ui.activity.main

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.ui.activity.BaseActivity
import com.crepetete.steamachievements.ui.common.helper.LoadingIndicator
import com.crepetete.steamachievements.ui.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.ui.fragment.library.LibraryFragment
import com.crepetete.steamachievements.ui.fragment.library.NavBarInteractionListener
import com.crepetete.steamachievements.ui.fragment.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.oshi.libsearchtoolbar.SearchAnimationToolbar
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject



/**
 *
 */
class MainActivity : BaseActivity(), LoadingIndicator,
    BottomNavigationView.OnNavigationItemSelectedListener, HasSupportFragmentInjector, SearchAnimationToolbar.OnSearchQueryChangedListener {

    private lateinit var toolbar: SearchAnimationToolbar

    companion object {
        fun getInstance(context: Context, userId: String) = Intent(context, MainActivity::class.java).apply {
            putExtra(INTENT_USER_ID, userId)
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

    //    private lateinit var loadingIndicator: PulsatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* Init search toolbar. */
        toolbar = findViewById(R.id.toolbar)
        toolbar.setSupportActionBar(this)
        toolbar.setOnSearchQueryChangedListener(this)

        /* Set a reference to the view responsible for showing a loader indicator. */
        //        loadingIndicator = findViewById(R.id.pulsator)

        handleIntent(intent)

        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.selectedItemId = R.id.menu_library
        navigation.setOnNavigationItemSelectedListener(this)

        navigation.selectedItemId = R.id.menu_library
    }

    override fun onNewIntent(intent: Intent?) {
        if (intent != null) {
            handleIntent(intent)
        }
    }

    override fun onBackPressed() {
        val handledByToolbar = toolbar.onBackPressed()

        if (!handledByToolbar) {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        if (itemId == R.id.action_search) {
            toolbar.onSearchIconClick()
            return true
        } else if (itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
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

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            navBarListener?.onSearchQueryUpdate(query)
        }
    }

    override fun onSearchCollapsed() {
        /* Do nothing. */
        onBackPressed()
    }

    override fun onSearchExpanded() {
        /* Do nothing. */
    }

    override fun onSearchQueryChanged(query: String?) {
        query?.let { q ->
            navBarListener?.onSearchQueryUpdate(q)
        }
    }

    override fun onSearchSubmitted(query: String?) {
        query?.let { q ->
            navBarListener?.onSearchQueryUpdate(q)
        }
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

    override fun supportFragmentInjector() = dispatchingAndroidInjector
}
