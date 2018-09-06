package com.crepetete.steamachievements.ui.activity.game

import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import dagger.Module
import dagger.Provides

@Module
class GameActivityModule {
    @Provides
    fun providesGameView(gameActivity: GameActivity): GameView = gameActivity

    @Provides
    fun providesGamePresenter(gameView: GameView,
                              gamesRepository: GamesRepository,
                              achievementsRepository: AchievementRepository): GamePresenter {
        return GamePresenter(gameView, gamesRepository, achievementsRepository)
    }
}