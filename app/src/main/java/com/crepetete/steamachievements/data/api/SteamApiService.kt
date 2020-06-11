package com.crepetete.data.network

import android.content.Context
import com.crepetete.data.network.response.base.ApiResponse
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.data.api.response.achievement.AchievedAchievementResponse
import com.crepetete.steamachievements.data.api.response.achievement.GlobalAchievResponse
import com.crepetete.steamachievements.data.api.response.game.BaseGameResponse
import com.crepetete.steamachievements.data.api.response.news.NewsResponse
import com.crepetete.steamachievements.data.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.data.api.response.user.UserResponse
import com.crepetete.steamachievements.util.livedata.LiveDataCallAdapterFactory
import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.ParseException
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Interface containing all possible calls that we need from the Steam API.
 *
 * The [BuildConfig.STEAM_API_KEY] is set up in the build.gradle file, which in its turn uses the
 * gradle.properties file.
 * This file should not be included in version control.
 * Details on how to obtain these values for yourself are described in the README.md and
 * in the build.gradle.
 */
interface SteamApiService {

    companion object {
        fun buildApiService(context: Context): SteamApiService {
            // Moshi
            val moshiConverterFactory =
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(object : Any() {
                            @ToJson
                            fun dateToJson(d: Date): Long {
                                return d.time
                            }

                            @FromJson
                            @Throws(ParseException::class)
                            fun dateToJson(s: Long): Date {
                                return Date(s)
                            }
                        })
                        .build()
                )

            // OkHttp
            val okHttpClient =
                OkHttpClient.Builder()
                    .cache(Cache(context.cacheDir, (10L * 1024L * 1024L)))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                        else HttpLoggingInterceptor.Level.NONE
                    })
                    .build()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.API_URL)
                .addConverterFactory(moshiConverterFactory)
                .addCallAdapterFactory(LiveDataCallAdapterFactory())
                .client(okHttpClient)
                .build()
                .create(SteamApiService::class.java)
        }
    }

    /**
     * Returns basic profile information for a list of 64-bit Steam IDs.
     *
     * Example URL: http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=<STEAM_API_KEY>&steamids=76561197960435530
     * (This will show Robin Walker's profile information.)
     *
     * @param id Comma-delimited list of 64 bit Steam IDs to return profile information for.
     *          Up to 100 Steam IDs can be requested.
     */
    @GET("/ISteamUser/GetPlayerSummaries/v0002/")
    fun getUserInfo(
        @Query("steamids") id: String,
        @Query("key") key: String = BuildConfig.STEAM_API_KEY
    ): ApiResponse<UserResponse>

    /**
     * Returns a list of games a player owns along with some playtime information, if the profile
     * is publicly visible.
     * Private, friends-only, and other privacy settings are not supported unless you are asking for
     * your own personal details (ie the WebAPI key you are using is linked to the steamId you are
     * requesting).
     *
     * Example URL:
     * http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=<STEAM_API_KEY>&steamid=76561197960434622&format=json
     *
     * @param id The SteamID of the account.
     * @param include_appinfo Include game name and logo information in the output. The default is
     *          to return appids only.
     * @param includeFreeGames By default, free games like Team Fortress 2 are excluded (as
     *          technically everyone owns them). If include_played_free_games is set, they will be
     *          returned if the player has played them at some point.
     *          This is the same behavior as the games list on the Steam Community.
     *
     * You can optionally filter the list to a set of appids. Note that these cannot be passed as a
     * URL parameter, instead you must use the JSON format described in
     * Steam_Web_API#Calling_Service_interfaces. The expected input is an array of integers
     * (in JSON: "appids_filter: [ 440, 500, 550 ]" )
     */
    @GET("/IPlayerService/GetOwnedGames/v0001/")
    suspend fun getGamesForUser(
        @Query("steamid") id: String,
        @Query("key") key: String = BuildConfig.STEAM_API_KEY,
        @Query("include_appinfo") includeAppInfo: Int = 1,
        @Query("include_played_free_games") includeFreeGames: Int = 1
    ): BaseGameResponse

    /**
     * GetSchemaForGame returns gamename, gameversion and availablegamestats(achievements and stats)
     *
     * Example URL:
     * http://api.steampowered.com/ISteamUserStats/GetSchemaForGame/v2/?key=<STEAM_API_KEY>&appid=218620
     *
     * @param appId The AppID of the game you want stats of
     */
    @GET("/ISteamUserStats/GetSchemaForGame/v2/")
    suspend fun getSchemaForGame(
        @Query("appid") appId: String,
        @Query("key") key: String = BuildConfig.STEAM_API_KEY
    ): SchemaResponse

    /**
     * Returns a list of achievements for this user by app id
     *
     * Example URL:
     * http://api.steampowered.com/ISteamUserStats/GetPlayerAchievements/v0001/?appid=440&key=<STEAM_API_KEY>&steamid=76561197972495328
     * @param id 64 bit Steam User ID to return achievements for.
     * @param appId The ID for the game you're requesting
     */
    @GET("/ISteamUserStats/GetPlayerAchievements/v0001/")
    suspend fun getAchievementsForPlayer(
        @Query("appid") appId: String,
        @Query("steamid") id: String,
        @Query("key") key: String = BuildConfig.STEAM_API_KEY
    ): AchievedAchievementResponse

    /**
     * Returns on global achievements overview of a specific game in percentages.
     *
     * Example URL:
     * http://api.steampowered.com/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/?gameid=440
     *
     * @param appId AppID of the game you want the percentages of.
     */
    @GET("/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/")
    suspend fun getGlobalAchievementStats(
        @Query("gameid") appId: String
    ): GlobalAchievResponse

    /**
     * GetNewsForApp returns the latest of a game specified by its appID.
     * Unless otherwise specified, the default number of returned news items is 3.
     *
     * Example URL:
     * http://api.steampowered.com/ISteamNews/GetNewsForApp/v0002/?appid=440&count=3&maxlength=300
     *
     * @param appId AppID of the game you want the news of.
     * @param count How many news entries you want to get returned.
     * @param maxLength Maximum length of each news entry.
     */
    @GET("/ISteamNews/GetNewsForApp/v0002/")
    fun getNews(
        @Query("appid") appId: String,
        @Query("count") count: String = "3",
        @Query("maxlength") maxLength: String = "10000",
        @Query("format") format: String = "json"
    ): NewsResponse
}
