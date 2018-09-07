package com.crepetete.steamachievements.injection.module

import android.content.SharedPreferences
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.database.dao.PlayerDao
import com.crepetete.steamachievements.data.repository.achievement.AchievementDataSource
import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GameRepository
import com.crepetete.steamachievements.data.repository.game.GamesDataSource
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import com.crepetete.steamachievements.data.repository.user.UserDataSource
import com.crepetete.steamachievements.data.repository.user.UserRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideUserRepository(sharedPreferences: SharedPreferences,
                              api: SteamApiService,
                              dao: PlayerDao): UserRepository =
            UserDataSource(sharedPreferences, api, dao)

    @Provides
    @Singleton
    fun provideGamesRepository(api: SteamApiService, gamesDao: GamesDao,
                               userRepository: UserRepository,
                               achievementsRepository: AchievementRepository)
            : GamesRepository = GamesDataSource(api, gamesDao, userRepository,
            achievementsRepository)

    @Provides
    @Singleton
    fun provideGameRepository(gamesDao: GamesDao): GameRepository = GameRepository(gamesDao)

    @Provides
    @Singleton
    fun provideAchievementRepository(api: SteamApiService,
                                     achievementsDao: AchievementsDao,
                                     userRep: UserRepository)
            : AchievementRepository = AchievementDataSource(api, achievementsDao, userRep)
}