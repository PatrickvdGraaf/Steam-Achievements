package com.crepetete.steamachievements.vo

import android.os.Parcelable
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Uses experimental feature [Parcelize], which removes boilerplate code.
 * More info;
 * @see <a href="https://proandroiddev.com/parcelable-in-kotlin-here-comes-parcelize-b998d5a5fcac">this link</a>.
 */
@Entity(tableName = "games",
    indices =[Index("appId")],
    foreignKeys = [(ForeignKey(
        entity = Player::class,
        parentColumns = ["steamId"],
        childColumns = ["userId"],
        onDelete = CASCADE))])
@Parcelize
data class Game(
    @PrimaryKey
    @Json(name = "appid")
    val appId: String,
    // TODO remove userId property, let a user have a list of game Ids instead.
    @ColumnInfo(name = "userId")
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
): Parcelable
