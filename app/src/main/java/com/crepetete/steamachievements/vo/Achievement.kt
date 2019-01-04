package com.crepetete.steamachievements.vo

import android.text.format.DateFormat
import androidx.room.Entity
import com.squareup.moshi.Json
import java.util.*

@Entity(tableName = "achievements",
        primaryKeys = ["name", "appId"])
data class Achievement(
        var appId: String,
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
) {
    private fun getDateString(): String {
        return if (unlockTime != null) {
            DateFormat.format("HH:mm\ndd-MM-yyyy", unlockTime).toString()
        } else {
            "Locked"
        }
    }

    fun getDateStringNoBreak(): String {
        return getDateString().replace("\n", " - ")
    }

    fun getDateStringNoTime(): String {
        return if (unlockTime != null) {
            DateFormat.format("dd-MM-yyyy", unlockTime).toString()
        } else {
            "Locked"
        }
    }
}

data class AchievementKeys(
        val appId: String,
        val name: String
)