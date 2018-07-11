package com.crepetete.steamachievements.base

import com.crepetete.steamachievements.injection.component.DaggerPresenterInjector
import com.crepetete.steamachievements.injection.component.PresenterInjector
import com.crepetete.steamachievements.injection.module.*
import com.crepetete.steamachievements.ui.activity.game.GamePresenter
import com.crepetete.steamachievements.ui.activity.login.LoginPresenter
import com.crepetete.steamachievements.ui.activity.main.MainPresenter
import com.crepetete.steamachievements.ui.fragment.achievements.AchievementPresenter
import com.crepetete.steamachievements.ui.fragment.library.LibraryPresenter
import com.crepetete.steamachievements.ui.fragment.profile.ProfilePresenter

abstract class BasePresenter<out V : BaseView>(protected val view: V) {
    /**
     * The injector used to inject required dependencies
     */
    private val injector: PresenterInjector = DaggerPresenterInjector
            .builder()
            .baseView(view)
            .apiModule(ApiModule)
            .contextModule(ContextModule)
            .dataModule(DataModule)
            .roomModule(RoomModule)
            .sharedPreferencesModule(SharedPreferencesModule)
            .build()

    init {
        inject()
    }

    /**
     * This method may be called when the presenter view is created
     */
    open fun onViewCreated() {}

    /**
     * This method may be called when the presenter view is destroyed
     */
    open fun onViewDestroyed() {}

    /**
     * Injects the required dependencies
     */
    private fun inject() {
        when (this) {
            is LoginPresenter -> injector.inject(this)
            is MainPresenter -> injector.inject(this)
            is GamePresenter -> injector.inject(this)
            is LibraryPresenter -> injector.inject(this)
            is ProfilePresenter -> injector.inject(this)
            is AchievementPresenter -> injector.inject(this)
            else -> throw NotImplementedError("No injection method implemented for" +
                    " ${this.javaClass.canonicalName}")
        }
    }
}