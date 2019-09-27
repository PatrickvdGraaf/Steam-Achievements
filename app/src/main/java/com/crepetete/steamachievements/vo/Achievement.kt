package com.crepetete.steamachievements.vo

import android.os.Parcelable
import androidx.room.Entity
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Uses experimental feature [Parcelize], which removes boilerplate code.
 * More info;
 * @see <a href="https://proandroiddev.com/parcelable-in-kotlin-here-comes-parcelize-b998d5a5fcac">this link</a>.
 */
@Entity(tableName = "achievements",
    primaryKeys = ["name", "appId"])
@Parcelize
data class Achievement(
    var appId: Long,
    val name: String,
    @Json(name = "defaultvalue")
    val defaultValue: Int,
    val displayName: String,
    val hidden: Int,
    var description: String?,
    @Json(name = "icon")
    val iconUrl: String,
    @Json(name = "icongray")
    val iconGrayUrl: String,
    var achieved: Boolean = false,
    var unlockTime: Date?,
    var updatedAt: Date?,
    var percentage: Float = 0.0f
) : Parcelable

data class AchievementKeys(
    val appId: Long,
    val name: String
)