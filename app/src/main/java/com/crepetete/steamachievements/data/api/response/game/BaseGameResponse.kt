package com.crepetete.steamachievements.data.api.response.game

import com.crepetete.steamachievements.model.Game
import com.squareup.moshi.Json

data class BaseGameResponse(val response: GamesResponse)

data class GamesResponse(
        @Json(name = "game_count")
        val count: Int,
        val games: List<Game>
)