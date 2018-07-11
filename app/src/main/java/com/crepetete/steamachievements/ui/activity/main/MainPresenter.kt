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

    @Inject
    lateinit var userRepository: UserRepository

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
                    view.showPlayerDetails(it.persona)
                }, {
                    Timber.e(it)
                }))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        var tag: String? = null
        val transaction = fragmentManager.beginTransaction()

        when (item.itemId) {
            R.id.menu_profile -> {
                tag = ProfileFragment.TAG
                fragment = fragmentManager.findFragmentByTag(tag)
                if (fragment == null) {
                    fragment = ProfileFragment.getInstance(playerId, this)
                }
            }
            R.id.menu_library -> {
                tag = LibraryFragment.TAG
                fragment = fragmentManager.findFragmentByTag(tag)
                if (fragment == null) {
                    fragment = LibraryFragment.getInstance(playerId, this)
                }
            }
            R.id.menu_achievements -> {
                tag = AchievementsFragment.TAG
                fragment = fragmentManager.findFragmentByTag(tag)
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

        transaction.replace(containerId, fragment, tag)
                .addToBackStack(null)
                .commit()
        return true
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