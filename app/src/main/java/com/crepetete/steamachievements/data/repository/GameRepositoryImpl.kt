package com.crepetete.steamachievements.data.repository

import androidx.lifecycle.LiveData
import com.crepetete.data.helper.NetworkBoundResource
import com.crepetete.data.helper.RateLimiter
import com.crepetete.data.network.SteamApiService
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.data.database.dao.AchievementsDao
import com.crepetete.steamachievements.data.database.dao.GamesDao
import com.crepetete.steamachievements.data.database.dao.NewsDao
import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.domain.repository.GameRepository
import com.crepetete.steamachievements.domain.usecases.achievements.UpdateAchievementsUseCase
import com.crepetete.steamachievements.testing.OpenForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OpenForTesting
class GameRepositoryImpl(
    private val api: SteamApiService,
    private val achievementsDao: AchievementsDao,
    private val gamesDao: GamesDao,
    private val newsDao: NewsDao,
    private val updateAchievementsUseCase: UpdateAchievementsUseCase
) : GameRepository {

    private companion object {
        const val FETCH_GAMES_KEY = "FETCH_GAMES_KEY"
        const val FETCH_NEWS_KEY = "FETCH_NEWS_KEY"
    }

    // Refresh games every day
    private val rateLimiter = RateLimiter<String>(1, TimeUnit.DAYS)

    /**
     * Fetch Games from both the Database and the API.
     * Refresh rate is set with the [rateLimiter].
     */
    override fun updateGames(userId: String?): LiveResource<List<Game>> {
        return object : NetworkBoundResource<List<Game>, List<BaseGameInfo>>() {

            override suspend fun saveCallResult(data: List<BaseGameInfo>) {
                gamesDao.upsert(data)

                updateAchievementsUseCase(userId, data.map { it.appId.toString() })
            }

            override fun shouldFetch(data: List<Game>?): Boolean {
                return rateLimiter.shouldFetch(FETCH_GAMES_KEY) || data == null
            }

            override suspend fun createCall(): List<BaseGameInfo>? {

//                gamesResponse?.let { baseGameInfo ->
//                    baseGameInfo.forEach { baseGame ->
//                        games.add(
//                            Game(
//                                baseGame,
//                                achievementsDao.getAchievements(baseGame.appId.toString())
//                            )
//                        )
//                    }
//                }

                return if (userId != null) {
                    api.getGamesForUser(userId).response.games
                } else {
                    listOf()
                }
            }

            override suspend fun loadFromDb(): List<Game> {
                return gamesDao.getGames()
            }
        }.asLiveResource()
    }

    override fun getGamesAsFlow(): Flow<List<BaseGameInfo>?> {
        return gamesDao.getGamesAsFlow()
    }

    /**
     * Fetch a specific [Game] form the Database.
     * Steam API doesn't provide a call for one specific game, so the data will only be the stored
     * value of the last API call.
     */
    override fun getGame(appId: String): LiveData<Game> {
        return gamesDao.getGame(appId)
    }

    override fun update(item: BaseGameInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            gamesDao.upsert(item)
        }
    }

    override fun getNews(appId: String): LiveResource<List<NewsItem>> {
        return object : NetworkBoundResource<List<NewsItem>, List<NewsItem>?>() {
            override suspend fun saveCallResult(data: List<NewsItem>?) {
                data?.let { news ->
                    newsDao.upsert(news)
                }
            }

            override fun shouldFetch(data: List<NewsItem>?): Boolean {
                return data == null || rateLimiter.shouldFetch(FETCH_NEWS_KEY)
            }

            override suspend fun loadFromDb(): List<NewsItem>? {
                return newsDao.getNewsForGame(appId)?.takeLast(3)
            }

            override suspend fun createCall(): List<NewsItem>? {
                return api.getNews(appId).appNews.newsItems
            }
        }.asLiveResource()
    }
}