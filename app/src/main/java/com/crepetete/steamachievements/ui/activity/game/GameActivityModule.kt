package com.crepetete.steamachievements.ui.activity.game

import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class GameActivityModule {
    @Binds
    abstract fun providesGameView(gameActivity: GameActivity): GameView

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesGamePresenter(gameView: GameView,
                                  gamesRepository: GamesRepository,
                                  achievementsRepository: AchievementRepository) = GamePresenter(gameView, gamesRepository, achievementsRepository)
    }
}