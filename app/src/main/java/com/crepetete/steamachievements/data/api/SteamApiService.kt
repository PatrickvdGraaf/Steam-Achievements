package com.crepetete.steamachievements.data.api

import com.crepetete.steamachievements.data.api.response.achievement.AchievedAchievementResponse
import com.crepetete.steamachievements.data.api.response.game.BaseGameResponse
import com.crepetete.steamachievements.data.api.response.schema.SchemaResponse
import com.crepetete.steamachievements.data.api.response.user.UserResponse
import com.crepetete.steamachievements.utils.API_KEY
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamApiService {
    @GET("ISteamUser/GetPlayerSummaries/v0002/")
    fun getUserInfo(@Query("steamids") id: String,
                    @Query("key") key: String = API_KEY): Single<UserResponse>

    @GET("IPlayerService/GetOwnedGames/v0001/")
    fun getGamesForUser(@Query("steamid") id: String,
                        @Query("key") key: String = API_KEY,
                        @Query("include_appinfo") includeAppInfo: Int = 1,
                        @Query("include_played_free_games") includeFreeGames: Int = 1)
            : Observable<BaseGameResponse>

    @GET("ISteamUserStats/GetSchemaForGame/v2/")
    fun getSchemaForGame(@Query("appid") appId: String,
                         @Query("key") key: String = API_KEY): Single<SchemaResponse>

    @GET("ISteamUserStats/GetPlayerAchievements/v0001/")
    fun getAchievementsForPlayer(@Query("appid") appId: String,
                                 @Query("steamid") id: String,
                                 @Query("key") key: String = API_KEY): Single<AchievedAchievementResponse>
}