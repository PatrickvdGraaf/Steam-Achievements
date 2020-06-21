package com.crepetete.steamachievements.util

import com.crepetete.steamachievements.domain.model.Player

object TestUtil {

    fun createPlayer(playerId: String) = Player(
        steamId = playerId,
        visibility = 3,
        profileState = 1,
        persona = "Crêpe Tête",
        lastLogOffInt = 1546612992,
        profileUrl = "https://steamcommunity.com/id/crepe-tete/",
        avatarSmallUrl = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/e6/e6e01b76f0eb48602d507e07184350e38c0c77af.jpg",
        avatarMediumUrl = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/e6/e6e01b76f0eb48602d507e07184350e38c0c77af_medium.jpg",
        avatarFullUrl = "https://steamcdn-a.akamaihd.net/steamcommunity/public/images/avatars/e6/e6e01b76f0eb48602d507e07184350e38c0c77af_full.jpg",
        personaState = 0,
        realName = "Patrick",
        primaryClanId = "103582791429521408",
        timeCreatedInt = 1322662289,
        personaStateFlags = 0,
        countryCode = "",
        cityId = null,
        stateCode = null
    )
}