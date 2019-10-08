package com.crepetete.steamachievements.api.response.game

import com.crepetete.steamachievements.vo.BaseGameInfo
import com.squareup.moshi.Json

data class BaseGameResponse(val response: GamesResponse)

data class GamesResponse(
    @Json(name = "game_count")
    val count: Int,
    val games: List<BaseGameInfo>
)