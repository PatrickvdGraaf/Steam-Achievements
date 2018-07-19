package com.crepetete.steamachievements.model

import android.arch.persistence.room.Entity
import android.text.format.DateFormat
import com.squareup.moshi.Json
import java.util.*

@Entity(tableName = "achievements",
        primaryKeys = ["name", "appId"])
//,
//foreignKeys = [(ForeignKey(
//entity = Game::class,
//parentColumns = ["appId"],
//childColumns = ["appId"],
//onDelete = ForeignKey.CASCADE))]
data class Achievement(
        var appId: String,
        val name: String,
        @Json(name = "defaultvalue")
        val defaultValue: Int,
        val displayName: String,
        val hidden: Int,
        val description: String?,
        @Json(name = "icon")
        val iconUrl: String,
        @Json(name = "icongray")
        val iconGrayUrl: String,
        var achieved: Boolean = false,
        var unlockTime: Date,
        var updatedAt: Date?
) {
    fun getDateString(): String {
        return DateFormat.format("hh:mm:ss a\ndd-MM-yyyy", unlockTime).toString()
    }
}

data class AchievementKeys(
        val appId: String,
        val name: String
)