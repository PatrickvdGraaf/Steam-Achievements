package com.crepetete.steamachievements.ui.fragment.library

import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import dagger.Module
import dagger.Provides

@Module
class LibraryFragmentModule {
    @Provides
    fun providesLibraryView(libraryFragment: LibraryFragment): LibraryView = libraryFragment

    @Provides
    fun providesLibraryPresenter(libraryView: LibraryView,
                                 gamesRepository: GamesRepository,
                                 achievementsRepository: AchievementRepository): LibraryPresenter {
        return LibraryPresenter(libraryView, gamesRepository, achievementsRepository)
    }
}