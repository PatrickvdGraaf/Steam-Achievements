package com.crepetete.steamachievements.injection.module

import android.content.Context
import android.content.SharedPreferences
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.database.dao.PlayerDao
import com.crepetete.steamachievements.data.repository.achievement.AchievementDataSource
import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesDataSource
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import com.crepetete.steamachievements.data.repository.user.UserDataSource
import com.crepetete.steamachievements.data.repository.user.UserRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Suppress("unused")
@Module
object DataModule {
    @Singleton
    @Provides
    internal fun provideUserRepository(context: Context,
                                       sharedPreferences: SharedPreferences,
                                       api: SteamApiService,
                                       dao: PlayerDao): UserRepository =
            UserDataSource(context, sharedPreferences, api, dao)

    @Singleton
    @Provides
    internal fun provideGamesRepository(api: SteamApiService, gamesDao: GamesDao,
                                        userRepository: UserRepository,
                                        achievementsRepository: AchievementRepository)
            : GamesRepository = GamesDataSource(api, gamesDao, userRepository,
            achievementsRepository)

    @Singleton
    @Provides
    internal fun provideAchievementRepository(context: Context,
                                              api: SteamApiService,
                                              achievementsDao: AchievementsDao,
                                              userRep: UserRepository)
            : AchievementRepository = AchievementDataSource(context, api, achievementsDao, userRep)
}