package com.crepetete.steamachievements.api

import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.api.response.ApiResponse
import com.crepetete.steamachievements.api.response.achievement.AchievedAchievementResponse
import com.crepetete.steamachievements.api.response.achievement.GlobalAchievResponse
import com.crepetete.steamachievements.api.response.game.BaseGameResponse
import com.crepetete.steamachievements.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.api.response.user.UserResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface containing all possible calls that we need from the Steam API.
 *
 * The [BuildConfig.STEAM_API_KEY] is set up in the build.gradle file, which in its turn uses the local.properties file.
 * This file should not be included in version control.
 * Details on how to obtain these values for yourself are described in build.gradle.
 */
interface SteamApiService {
    @GET("/ISteamUser/GetPlayerSummaries/v0002/")
    suspend fun getUserInfo(
        @Query("steamids") id: String,
        @Query("key") key: String = BuildConfig.STEAM_API_KEY
    ): ApiResponse<UserResponse>

    @GET("/IPlayerService/GetOwnedGames/v0001/")
    suspend fun getGamesForUser(
        @Query("steamid") id: String,
        @Query("key") key: String = BuildConfig.STEAM_API_KEY,
        @Query("include_appinfo") includeAppInfo: Int = 1,
        @Query("include_played_free_games") includeFreeGames: Int = 1
    ): BaseGameResponse

    @GET("/ISteamUserStats/GetSchemaForGame/v2/")
    suspend fun getSchemaForGame(
        @Query("appid") appId: String,
        @Query("key") key: String = BuildConfig.STEAM_API_KEY
    ): SchemaResponse

    @GET("/ISteamUserStats/GetPlayerAchievements/v0001/")
    suspend fun getAchievementsForPlayer(
        @Query("appid") appId: String,
        @Query("steamid") id: String,
        @Query("key") key: String = BuildConfig.STEAM_API_KEY
    ): AchievedAchievementResponse

    @GET("/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/")
    suspend fun getGlobalAchievementStats(
        @Query("gameid") appId: String
    ): GlobalAchievResponse
}