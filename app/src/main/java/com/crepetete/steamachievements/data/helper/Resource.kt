package com.crepetete.steamachievements.data.helper

import java.util.Date

/**
 * A generic class that describe data with a status using Sealed classes in Kotlin
 * (rather than using enum).
 * https://gist.github.com/AliMehrpour/e4787020fe2356ad9301820e74adc4d4
 *
 * Custom object based that allows us to emit the loading status without setting the data.
 * https://medium.com/ideas-by-idean/android-adventure-512bbd78b05f
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 24 Sep, 2019; 21:50.
 */

data class Resource<out T>(
    val status: Status,
    val data: T? = null,
    val message: String? = null,
    val date: Date? = null
) {
    companion object {
        fun <T> success(data: T?, date: Date?): Resource<T> {
            return Resource(Status.SUCCESS, data, date = date)
        }

        fun <T> error(msg: String, data: T?, date: Date?): Resource<T> {
            return Resource(Status.ERROR, data, msg, date)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(status = Status.LOADING, data = data)
        }

        fun <T> cached(data: T?, date: Date?): Resource<T> {
            return Resource(status = Status.CACHED, data = data, date = date)
        }

        fun <T> reAuthenticate(): Resource<T> {
            return Resource(Status.REAUTH)
        }

        fun <T> logout(): Resource<T> {
            return Resource(Status.LOGOUT)
        }
    }
}

enum class Status {
    SUCCESS, ERROR, LOADING, CACHED, REAUTH, LOGOUT
}