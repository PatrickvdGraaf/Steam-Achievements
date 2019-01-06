package com.crepetete.steamachievements.vo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import java.util.*

@Entity(tableName = "games",
    foreignKeys = [(ForeignKey(
        entity = Player::class,
        parentColumns = ["steamId"],
        childColumns = ["userId"],
        onDelete = CASCADE))])
data class Game(
    @PrimaryKey
    @Json(name = "appid")
    val appId: String,
    // TODO remove userId property, let a user have a list of game Ids instead.
    var userId: String,
    val name: String,
    @Json(name = "playtime_2weeks")
    val recentPlayTime: Long,
    @Json(name = "playtime_forever")
    val playTime: Long,
    @Json(name = "img_icon_url")
    val iconUrl: String,
    @Json(name = "img_logo_url")
    val logoUrl: String,
    // Custom variables.
    var colorPrimaryDark: Int = 0, // Color extracted from overall banner image color Used to set colors in views.
    var lastUpdated: Long = Calendar.getInstance().time.time // Timer on updates which can be used to determine refreshes.
)
