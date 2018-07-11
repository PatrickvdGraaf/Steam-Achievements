package com.crepetete.steamachievements.utils

import android.arch.persistence.room.TypeConverter
import java.util.*


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time ?: 0
    }
}