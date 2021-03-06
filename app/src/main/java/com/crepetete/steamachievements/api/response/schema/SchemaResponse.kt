package com.crepetete.steamachievements.api.response.schema

import com.crepetete.steamachievements.vo.Achievement

data class SchemaResponse(val game: GameResponse)

data class GameResponse(
        val gameName: String?,
        val gameVersion: String?,
        val availableGameStats: Stats?)

data class Stats(val achievements: MutableList<Achievement>)