package com.crepetete.steamachievements.ui.fragment.achievements

import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class AchievementsFragmentModule {
    @Binds
    abstract fun providesAchievementsView(achievementsFragment: AchievementsFragment): AchievementsView

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesAchievementPresenter(achievementsView: AchievementsView,
                                         achievementsRepository: AchievementRepository,
                                         gamesRepository: GamesRepository) = AchievementPresenter(achievementsView, achievementsRepository, gamesRepository)
    }
}