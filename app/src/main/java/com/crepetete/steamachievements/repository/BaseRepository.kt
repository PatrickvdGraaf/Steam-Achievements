package com.crepetete.steamachievements.repository

import timber.log.Timber

/**
 * @author: Patrick van de Graaf.
 * @date: Mon 23 Sep, 2019; 11:02.
 */
abstract class BaseRepository {
    fun logThread(methodName: String) {
        Timber.d("debug: $methodName: ${Thread.currentThread().name}.")
    }
}