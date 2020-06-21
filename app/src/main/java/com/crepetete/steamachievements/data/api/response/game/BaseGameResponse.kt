package com.crepetete.steamachievements.data.api.response.game

import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.squareup.moshi.Json

data class BaseGameResponse(@field:Json(name = "response") val gamesResponse: GamesResponse)

data class GamesResponse(
    @field:Json(name = "game_count")
    val count: Int,
    @field:Json(name = "games")
    val games: List<BaseGameInfo>?
)