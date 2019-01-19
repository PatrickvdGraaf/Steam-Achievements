package com.crepetete.steamachievements.api

import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.api.response.ApiResponse
import com.crepetete.steamachievements.api.response.achievement.AchievedAchievementResponse
import com.crepetete.steamachievements.api.response.achievement.GlobalAchievResponse
import com.crepetete.steamachievements.api.response.game.BaseGameResponse
import com.crepetete.steamachievements.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.api.response.user.UserResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApiService {
    @GET("/ISteamUser/GetPlayerSummaries/v0002/")
    fun getUserInfo(
        @Query("steamids") id: String,
        @Query("key") key: String = API_KEY
    ): LiveData<ApiResponse<UserResponse>>

    @GET("/IPlayerService/GetOwnedGames/v0001/")
    fun getGamesForUser(
        @Query("steamid") id: String,
        @Query("key") key: String = API_KEY,
        @Query("include_appinfo") includeAppInfo: Int = 1,
        @Query("include_played_free_games") includeFreeGames: Int = 1
    ): LiveData<ApiResponse<BaseGameResponse>>

    @GET("/ISteamUserStats/GetSchemaForGame/v2/")
    fun getSchemaForGame(
        @Query("appid") appId: String,
        @Query("key") key: String = API_KEY
    ): LiveData<ApiResponse<SchemaResponse>>

    @GET("/ISteamUserStats/GetSchemaForGame/v2/")
    fun getSchemaForGameAsSingle(
        @Query("appid") appId: String,
        @Query("key") key: String = API_KEY
    ): Single<SchemaResponse>

    @GET("/ISteamUserStats/GetPlayerAchievements/v0001/")
    fun getAchievementsForPlayer(
        @Query("appid") appId: String,
        @Query("steamid") id: String,
        @Query("key") key: String = API_KEY
    ): LiveData<ApiResponse<AchievedAchievementResponse>>

    @GET("/ISteamUserStats/GetPlayerAchievements/v0001/")
    fun getAchievementsForPlayerAsSingle(
        @Query("appid") appId: String,
        @Query("steamid") id: String,
        @Query("key") key: String = API_KEY
    ):Single<AchievedAchievementResponse>

    @GET("/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/")
    fun getGlobalAchievementStats(
        @Query("gameid") appId: String
    ): LiveData<ApiResponse<GlobalAchievResponse>>

    @GET("/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v0002/")
    fun getGlobalAchievementStatsAsSingle(
        @Query("gameid") appId: String
    ): Single<GlobalAchievResponse>
}