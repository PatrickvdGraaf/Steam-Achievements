package com.crepetete.steamachievements.ui.common

import android.support.annotation.IntDef

@IntDef(PLAYTIME, NAME, COMPLETION)
@Retention(AnnotationRetention.SOURCE)
annotation class SortingType

const val PLAYTIME = 0
const val NAME = 1
const val COMPLETION = 2