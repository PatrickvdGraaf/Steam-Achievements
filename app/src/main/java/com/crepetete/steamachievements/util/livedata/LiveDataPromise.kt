package com.crepetete.steamachievements.util.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

/**
 * Concatenate LiveData
 *
 * Created at 24 January, 2019.
 */
fun <T, R> LiveData<T>.then(callback: (T) -> LiveData<R>): LiveData<R> {
    return Transformations.switchMap(this) {
        callback(it)
    }
}
