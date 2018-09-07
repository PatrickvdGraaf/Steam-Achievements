package com.crepetete.steamachievements.injection

import android.app.Application
import android.arch.persistence.room.Room
import android.content.Context
import android.content.SharedPreferences
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.data.api.SteamApiService
import com.crepetete.steamachievements.data.database.SteamDatabase
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
import com.crepetete.steamachievements.injection.module.ViewModelModule
import com.crepetete.steamachievements.utils.BASE_URL
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.ParseException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


/**
 * We provide retrofit, okhttp, persistence db, shared pref etc here. There is an important detail
 * here. We have to add our subcomponents to AppModule.
 */
@Module(includes = [ViewModelModule::class])
class AppModule {
    /*
    API Singletons
     */
    @Provides
    @Singleton
    fun provideRetrofitInterface(moshiConverterFactory: MoshiConverterFactory,
                                 rxJava2CallAdapterFactory: RxJava2CallAdapterFactory,
                                 okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(moshiConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .client(okHttpClient)
                .build()
    }

    /**
     * Provides an OkHttpClient with some base values and an Interceptor for debugging purposes.
     * @return the OkHttpClient
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache): OkHttpClient {
        val client = OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
                })
        return client.build()
    }

    @Provides
    @Singleton
    fun providesOkHttpCache(context: Context): Cache {
        val cacheSize = 10L * 1024L * 1024L // 10MB
        return Cache(context.cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    fun provideMoshiConverterFactory(): MoshiConverterFactory {
        val customDateAdapter = object : Any() {
            @ToJson
            fun dateToJson(d: Date): Long {
                return d.time
            }

            @FromJson
            @Throws(ParseException::class)
            fun dateToJson(s: Long): Date {
                return Date(s)
            }
        }

        val moshi = Moshi.Builder()
                .add(customDateAdapter)
                .build()

        return MoshiConverterFactory.create(moshi)
    }

    @Provides
    @Singleton
    fun providesRxJavaCallAdapterFactory(): RxJava2CallAdapterFactory {
        return RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())
    }

    /**
     * Provides the Post service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the Post service implementation.
     */
    @Provides
    @Singleton
    internal fun provideSteamApi(retrofit: Retrofit): SteamApiService {
        return retrofit.create(SteamApiService::class.java)
    }

    /*
    Context Singletons
     */
    /**
     * Provides the Context
     * @return the Context to be provided
     */
    @Provides
    fun provideContext(application: Application): Context = application.applicationContext

    /*
    Repository Singletons
     */
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

    /*
    Room Singletons
     */
    @Provides
    @Singleton
    internal fun providePlayerDatabase(application: Application) = Room
            .databaseBuilder(application, SteamDatabase::class.java, "players.db")
            .build()

    @Provides
    @Singleton
    internal fun providePlayerDao(database: SteamDatabase): PlayerDao = database.playerDao()

    @Provides
    @Singleton
    internal fun provideGamesDao(database: SteamDatabase): GamesDao = database.gamesDao()

    @Provides
    @Singleton
    internal fun provideAchievementsDao(database: SteamDatabase): AchievementsDao =
            database.achievementsDao()

    /*
    Shared Preferences
     */
    @Provides
    @Singleton
    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    }
}