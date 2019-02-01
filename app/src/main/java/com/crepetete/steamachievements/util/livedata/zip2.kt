package com.crepetete.steamachievements.util.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * zip2 function takes two LiveData, src1 and src2. These are used as input source for MediatorLiveData to create.
 * And also it takes zipper function as its last parameter, and it should describe how the data would be zipped into
 * one object R. In the updateValueIfNeeded() function, it checks version and null, and then set MediatorLiveData’s value.
 *
 * Only supports two live data inputs for now.
 *
 * Created at 01 February, 2019.
 */

fun <T1, T2, R> zip2(
    src1: LiveData<T1>,
    src2: LiveData<T2>,
    zipper: (T1, T2) -> R): LiveData<R> {

    return MediatorLiveData<R>().apply {
        var src1Version = 0
        var src2Version = 0

        var lastSrc1: T1? = null
        var lastSrc2: T2? = null

        fun updateValueIfNeeded() {
            if (src1Version > 0 && src2Version > 0 &&
                lastSrc1 != null && lastSrc2 != null) {
                value = zipper(lastSrc1!!, lastSrc2!!)
                src1Version = 0
                src2Version = 0
            }
        }

        addSource(src1) {
            lastSrc1 = it
            src1Version++
            updateValueIfNeeded()
        }

        addSource(src2) {
            lastSrc2 = it
            src2Version++
            updateValueIfNeeded()
        }
    }
}

fun <T1, T2, T3, R> zip3(
    src1: LiveData<T1>,
    src2: LiveData<T2>,
    src3: LiveData<T3>,
    zipper: (T1, T2, T3) -> R): LiveData<R> {

    return MediatorLiveData<R>().apply {
        var src1Version = 0
        var src2Version = 0
        var src3Version = 0

        var lastSrc1: T1? = null
        var lastSrc2: T2? = null
        var lastSrc3: T3? = null

        fun updateValueIfNeeded() {
            if (src1Version > 0 && src2Version > 0 && src3Version > 0 &&
                lastSrc1 != null && lastSrc2 != null && lastSrc3 != null) {
                value = zipper(lastSrc1!!, lastSrc2!!, lastSrc3!!)
                src1Version = 0
                src2Version = 0
                src3Version = 0
            }
        }

        addSource(src1) {
            lastSrc1 = it
            src1Version++
            updateValueIfNeeded()
        }

        addSource(src2) {
            lastSrc2 = it
            src2Version++
            updateValueIfNeeded()
        }

        addSource(src3) {
            lastSrc3 = it
            src3Version++
            updateValueIfNeeded()
        }
    }
}