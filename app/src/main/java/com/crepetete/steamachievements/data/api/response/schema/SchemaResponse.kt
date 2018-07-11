package com.crepetete.steamachievements.data.api.response.schema

import com.crepetete.steamachievements.model.Achievement

data class SchemaResponse(val game: GameResponse)

data class GameResponse(
        val gameName: String?,
        val gameVersion: String?,
        val availableGameStats: Stats?)

data class Stats(val achievements: List<Achievement>)