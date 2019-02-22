package com.crepetete.steamachievements.vo

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
data class Resource<out T>(val status: Status, val data: T? = null, val message: String? = null) {
    companion object {
        /**
         * Creates [Resource] object with `SUCCESS` status and [data].
         */
        fun <T> success(data: T?): Resource<T> = Resource(Status.SUCCESS, data)

        /**
         * Creates [Resource] object with `ERROR` status and [message].
         */
        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg)
        }

        /**
         * Creates [Resource] object with `LOADING` status to notify
         * the UI to showing loading.
         */
        fun <T> loading(): Resource<T> {
            return Resource(Status.LOADING)
        }
    }
}