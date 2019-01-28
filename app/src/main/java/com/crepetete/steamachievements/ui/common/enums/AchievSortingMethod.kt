package com.crepetete.steamachievements.ui.common.enums

import android.content.res.Resources
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.vo.Achievement
import timber.log.Timber

/**
 *
 * Order class with concrete sorting orders, which implement [Comparable].
 * These are used for nicely ordering lists of specific classes.
 *
 * Created by Patrick van de Graaf on 7/20/2018.
 *
 */

class Order {
    interface BaseComparator<T> : Comparator<T> {
        fun getName(resources: Resources): String
    }

    class AchievedOrder : BaseComparator<Achievement> {
        override fun compare(o1: Achievement, o2: Achievement) = try {
            if (o1.unlockTime != null && o2.unlockTime != null) {
                when {
                    o1.unlockTime == o2.unlockTime -> 0
                    o1.unlockTime!!.after(o2.unlockTime) -> -1
                    else -> 1
                }
            } else if (o1.unlockTime == null && o2.unlockTime != null) {
                1
            } else if (o1.unlockTime == null && o2.unlockTime == null) {
                0
            } else {
                -1
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while comparing Achievments in AchievedOrder")
            0
        }

        override fun getName(resources: Resources): String = resources.getString(R.string.sorting_order_achieved_name)
    }

    class NotAchievedOrder : BaseComparator<Achievement> {
        override fun compare(o1: Achievement?, o2: Achievement?) = try {
            if (o1?.unlockTime != null && o2?.unlockTime != null) {
                when {
                    o1.unlockTime == o2.unlockTime -> 0
                    o1.unlockTime!!.after(o2.unlockTime) -> 1
                    else -> -1
                }
            } else if (o1?.unlockTime == null && o2?.unlockTime != null) {
                -1
            } else if (o1?.unlockTime == null && o2?.unlockTime == null) {
                0
            } else {
                1
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while comparing Achievments in NotAchievedOrder")
            0
        }

        override fun getName(resources: Resources): String = resources.getString(R.string.sorting_order_not_achieved)
    }

    class RarityOrder : BaseComparator<Achievement> {
        override fun compare(o1: Achievement?, o2: Achievement?) = when {
            o1?.percentage ?: 0F > o2?.percentage ?: 0F -> 1
            o1?.percentage ?: 0F < o2?.percentage ?: 0F -> -1
            else -> 0
        }

        override fun getName(resources: Resources): String = resources.getString(R.string.sorting_order_rarity)
    }
}