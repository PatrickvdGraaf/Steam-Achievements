package com.crepetete.steamachievements.injection.component

import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.injection.module.*
import com.crepetete.steamachievements.ui.activity.game.GamePresenter
import com.crepetete.steamachievements.ui.activity.login.LoginPresenter
import com.crepetete.steamachievements.ui.activity.main.MainPresenter
import com.crepetete.steamachievements.ui.fragment.achievements.AchievementPresenter
import com.crepetete.steamachievements.ui.fragment.library.LibraryPresenter
import com.crepetete.steamachievements.ui.fragment.profile.ProfilePresenter
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

/**
 * Component providing inject() methods for presenters.
 */
@Singleton
@Component(modules = [(ApiModule::class), (ContextModule::class), (DataModule::class),
    (RoomModule::class), (SharedPreferencesModule::class)])
interface PresenterInjector {
    /**
     * Injects required dependencies into the specified Presenter.
     */
    fun inject(loginPresenter: LoginPresenter)

    fun inject(mainPresenter: MainPresenter)

    fun inject(gamePresenter: GamePresenter)

    fun inject(libraryPresenter: LibraryPresenter)

    fun inject(profilePresenter: ProfilePresenter)

    fun inject(achievementPresenter: AchievementPresenter)

    @Component.Builder
    interface Builder {
        fun build(): PresenterInjector

        fun roomModule(roomModule: RoomModule): Builder
        fun dataModule(dataModule: DataModule): Builder
        fun contextModule(contextModule: ContextModule): Builder
        fun apiModule(apiModule: ApiModule): Builder
        fun sharedPreferencesModule(sharedPreferencesModule: SharedPreferencesModule): Builder

        @BindsInstance
        fun baseView(baseView: BaseView): Builder
    }
}