package com.crepetete.steamachievements.domain.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

/**
 * Uses experimental feature [Parcelize], which removes boilerplate code.
 * More info;
 * @see <a href="https://proandroiddev.com/parcelable-in-kotlin-here-comes-parcelize-b998d5a5fcac">this link</a>.
 */
@Entity(
    tableName = "games",
    indices = [Index("appId")]
)
@Parcelize
data class BaseGameInfo(
    @PrimaryKey
    @ColumnInfo(name = "appId")
    @field:Json(name = "appid")
    val appId: Int,
    @field:Json(name = "name")
    val name: String,
    @field:Json(name = "playtime_2weeks")
    val recentPlayTime: Int?,
    @field:Json(name = "playtime_forever")
    val playTime: Int,
    @field:Json(name = "img_icon_url")
    val iconUrl: String,
    @field:Json(name = "img_logo_url")
    val logoUrl: String,
    // Custom variables.
    var colorPrimaryDark: Int?, // Color extracted from overall banner image color Used to set colors in views.
    var lastUpdated: Long? // Timer on updates which can be used to determine refreshes.
) : Parcelable {
    fun getBannerUrl() =
        "http://media.steampowered.com/steamcommunity/public/images/apps" +
                "/$appId" +
                "/$logoUrl" +
                ".jpg"
}
