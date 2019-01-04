package com.crepetete.steamachievements.vo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "players")
data class Player(
        @PrimaryKey
        @Json(name = "steamid")
        val steamId: String,
        @Json(name = "communityvisibilitystate")
        val visibility: Int,
        @Json(name = "profilestate")
        val profileState: Int,
        @Json(name = "personaname")
        val persona: String,
        @Json(name = "lastlogoff")
        val lastLogOffInt: Int,
        @Json(name = "profileurl")
        val profileUrl: String,
        @Json(name = "avatar")
        val avatarSmallUrl: String,
        @Json(name = "avatarmedium")
        val avatarMediumUrl: String,
        @Json(name = "avatarfull")
        val avatarFullUrl: String,
        @Json(name = "personastate")
        val personaState: Int,
        @Json(name = "realname")
        val realName: String,
        @Json(name = "primaryclanid")
        val primaryClanId: String,
        @Json(name = "timecreated")
        val timeCreatedInt: Int,
        @Json(name = "personastateflags")
        val personaStateFlags: Int,
        @Json(name = "loccountrycode")
        val countryCode: String?,
        @Json(name = "locstatecode")
        val stateCode: String?,
        @Json(name = "loccityid")
        val cityId: Int?
)