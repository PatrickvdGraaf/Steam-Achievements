package com.crepetete.steamachievements.data.helper

import androidx.annotation.IntDef
import androidx.lifecycle.LiveData
import com.crepetete.steamachievements.data.helper.LiveResource.Companion.STATE_FAILED
import com.crepetete.steamachievements.data.helper.LiveResource.Companion.STATE_LOADING
import com.crepetete.steamachievements.data.helper.LiveResource.Companion.STATE_SUCCESS
import kotlinx.coroutines.Job

/**
 * Custom object based that allows us to emit the loading status without setting the data.
 * https://medium.com/ideas-by-idean/android-adventure-512bbd78b05f
 *
 * @author: Patrick van de Graaf.
 * @date: Tue 24 Sep, 2019; 21:50.
 */
@Target(
    AnnotationTarget.TYPE,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY
)
@Retention(AnnotationRetention.SOURCE)
@IntDef(STATE_LOADING, STATE_SUCCESS, STATE_FAILED)
annotation class ResourceState

data class LiveResource<T>(
    val data: LiveData<T?>,
    val state: LiveData<@ResourceState Int?>,
    val error: LiveData<Exception?>,
    val job: Job? = null
) {
    companion object {
        const val STATE_LOADING = 1
        const val STATE_SUCCESS = 2
        const val STATE_FAILED = 0
    }
}