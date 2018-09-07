package com.crepetete.steamachievements.ui.fragment.library

import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import dagger.Module
import dagger.Provides

@Module
class LibraryFragmentModule {
    @Provides
    fun providesLibraryView(libraryFragment: LibraryFragment): LibraryView = libraryFragment

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesLibraryPresenter(libraryView: LibraryView,
                                     gamesRepository: GamesRepository,
                                     achievementsRepository: AchievementRepository) = LibraryPresenter(libraryView, gamesRepository, achievementsRepository)
    }
}