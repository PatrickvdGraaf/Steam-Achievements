package com.crepetete.steamachievements.ui.fragment.achievements

import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import dagger.Module
import dagger.Provides

@Module
class AchievementsFragmentModule {
    @Provides
    fun providesAchievementsView(achievementsFragment: AchievementsFragment): AchievementsView {
        return achievementsFragment
    }

    @Provides
    fun providesAchievementPresenter(achievementsView: AchievementsView,
                                 achievementsRepository: AchievementRepository,
                                 gamesRepository: GamesRepository): AchievementPresenter {
        return AchievementPresenter(achievementsView, achievementsRepository, gamesRepository)
    }
}