package com.crepetete.steamachievements.util.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Fri 04 Oct, 2019; 19:18.
 */
class CombinedLiveData<T, K, S>(
    source1: LiveData<T?>,
    source2: LiveData<K?>,
    private val combine: (data1: T?, data2: K?) -> S
) : MediatorLiveData<S>() {

    private var data1: T? = null
    private var data2: K? = null

    init {
        super.addSource(source1) {
            data1 = it
            value = combine(data1, data2)
        }
        super.addSource(source2) {
            data2 = it
            value = combine(data1, data2)
        }
    }

    override fun <T : Any?> addSource(source: LiveData<T?>, onChanged: Observer<in T?>) {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> removeSource(toRemote: LiveData<T?>) {
        throw UnsupportedOperationException()
    }
}