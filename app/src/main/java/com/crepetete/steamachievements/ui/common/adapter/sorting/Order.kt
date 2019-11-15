package com.crepetete.steamachievements.ui.common.adapter.sorting

import android.content.res.Resources
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.vo.Achievement
import timber.log.Timber
import java.util.*

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

    class LatestAchievedOrder : BaseComparator<Achievement> {

        private val steamReleaseDate = Calendar.getInstance().apply {
            set(2003, 9, 12, 0, 0, 0)
        }.time

        override fun compare(o1: Achievement, o2: Achievement) = try {
            val unlockTime1 = o1.unlockTime
            val unlockTime2 = o2.unlockTime

            if (unlockTime1?.after(steamReleaseDate) == true && unlockTime2?.after(steamReleaseDate) == true) {
                when {
                    unlockTime1.after(unlockTime2) -> -1
                    unlockTime1.before(unlockTime2) -> 1
                    else -> 0
                }
            } else if (unlockTime1?.after(steamReleaseDate) == true && unlockTime2?.after(
                    steamReleaseDate
                ) == false
            ) {
                -1
            } else if (unlockTime1?.after(steamReleaseDate) == false && unlockTime2?.after(
                    steamReleaseDate
                ) == true
            ) {
                1
            } else {
                0
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while comparing Achievments in LatestAchievedOrder")
            0
        }

        override fun getName(resources: Resources): String =
            resources.getString(R.string.sorting_order_achieved_name)
    }

    class NotAchievedOrder : BaseComparator<Achievement> {
        override fun compare(o1: Achievement?, o2: Achievement?) = try {
            if (o1 != null && o2 != null) {
                when {
                    o1.achieved && !o2.achieved -> 1
                    !o1.achieved && o2.achieved -> -1
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

        override fun getName(resources: Resources): String =
            resources.getString(R.string.sorting_order_not_achieved)
    }

    class RarityOrder : BaseComparator<Achievement> {
        override fun compare(o1: Achievement?, o2: Achievement?) = when {
            o1?.percentage ?: 0F > o2?.percentage ?: 0F -> 1
            o1?.percentage ?: 0F < o2?.percentage ?: 0F -> -1
            else -> 0
        }

        override fun getName(resources: Resources): String =
            resources.getString(R.string.sorting_order_rarity)
    }
}