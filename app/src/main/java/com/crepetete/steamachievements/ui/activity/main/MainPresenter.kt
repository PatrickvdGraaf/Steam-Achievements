package com.crepetete.steamachievements.ui.activity.main

import android.support.annotation.IdRes
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.MenuItem
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.user.UserRepository
import com.crepetete.steamachievements.ui.activity.helper.LoadingIndicator
import com.crepetete.steamachievements.ui.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.ui.fragment.library.LibraryFragment
import com.crepetete.steamachievements.ui.fragment.library.NavbarInteractionListener
import com.crepetete.steamachievements.ui.fragment.profile.ProfileFragment
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class MainPresenter(mainView: MainView,
                    @IdRes private val containerId: Int,
                    private val fragmentManager: FragmentManager,
                    private val playerId: String) : BasePresenter<MainView>(mainView),
        BottomNavigationView.OnNavigationItemSelectedListener, LoadingIndicator {
    private var disposable: CompositeDisposable = CompositeDisposable()

    private var navBarListener: NavbarInteractionListener? = null

    private var persona = "Unknown"

    @IdRes
    private var selectedNavItem = R.id.menu_library

    @Inject
    lateinit var userRepository: UserRepository

    private var currentTag = LibraryFragment.TAG

    override fun onViewCreated() {
        getPlayer()

        val fragment: LibraryFragment = LibraryFragment.getInstance(playerId, this)
        fragmentManager.beginTransaction()
                .replace(containerId, fragment, LibraryFragment.TAG)
                .addToBackStack(null)
                .commit()
        navBarListener = fragment
    }

    private fun getPlayer() {
        disposable.add(userRepository.getPlayer(playerId)
                .subscribe({
                    persona = it.persona
                    updateTitle()
                }, {
                    Timber.e(it)
                }))
    }

    private fun updateTitle() {
        when (selectedNavItem) {
            R.id.menu_profile -> view.setTitle("Profile")
            R.id.menu_achievements -> view.setTitle("$persona's Achievements")
            else -> view.setTitle("$persona's Library")
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
                    fragment = ProfileFragment.getInstance(playerId, this)
                }
            }
            R.id.menu_library -> {
                currentTag = LibraryFragment.TAG
                fragment = fragmentManager.findFragmentByTag(currentTag)
                if (fragment == null) {
                    fragment = LibraryFragment.getInstance(playerId, this)
                }
            }
            R.id.menu_achievements -> {
                currentTag = AchievementsFragment.TAG
                fragment = fragmentManager.findFragmentByTag(currentTag)
                if (fragment == null) {
                    fragment = AchievementsFragment.getInstance(playerId, this)
                }
            }
        }

        navBarListener = if (fragment is NavbarInteractionListener) {
            fragment
        } else {
            null
        }

        updateTitle()

        transaction.replace(containerId, fragment, currentTag)
                .addToBackStack(null)
                .commit()
        return true
    }

    fun onRefreshClicked() {
        val fragment = fragmentManager.findFragmentByTag(currentTag)

    }

    fun onSearchQueryChanged(query: String) {
        navBarListener?.onSearchQueryUpdate(query)
    }

    fun onSortingMerhodChanged(method: Int) {
        navBarListener?.onSortingMethodChanged(method)
    }

    override fun showLoading() {
        view.showLoading()
    }

    override fun hideLoading() {
        view.hideLoading()
    }

    override fun onViewDestroyed() {
        disposable.dispose()
    }
}