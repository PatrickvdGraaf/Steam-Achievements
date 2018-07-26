package com.crepetete.steamachievements.ui.fragment.profile

import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.user.UserRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ProfilePresenter(profileView: ProfileView) : BasePresenter<ProfileView>(profileView) {
    @Inject
    lateinit var userRepository: UserRepository

    override fun onViewCreated() {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        disposable.add(userRepository.getCurrentPlayer()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != null) {
                        view.onPlayerLoaded(it)
                    } else {
                        // TODO go to login page.
                    }
                }, {
                    Timber.e(it)
                }))
    }
}