package com.crepetete.steamachievements.ui.common.adapter.diffutil

import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import com.crepetete.steamachievements.vo.Game

/**
 * Created at 19 January, 2019.
 */
class GamesDiffCallback(
    private val oldList: List<Game>?,
    private val newList: List<Game>?
) : DiffUtil.Callback() {

    companion object {
        const val KEY_NAME = "KEY_NAME"
        const val KEY_TOTAL_PLAYTIME = "KEY_TOTAL_PLAYTIME"
        const val KEY_PERCENTAGE = "KEY_PERCENTAGE"
        const val KEY_ACHIEVEMENTS_UPDATED = "KEY_ACHIEVEMENTS_UPDATED"
    }

    override fun getOldListSize() = oldList?.size ?: 0

    override fun getNewListSize() = newList?.size ?: 0

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = newList
        ?.get(newItemPosition)?.getAppId() == oldList?.get(oldItemPosition)?.getAppId()

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = newList
        ?.get(newItemPosition)?.equals(oldList?.get(oldItemPosition)) ?: false

    /**
     *  This method is called when the areItemsTheSame() returns true, but areContentsTheSame()
     *  returns false, which means that we are talking about the same item but the fields data
     *  might have changed.
     *  Basically, this method returns the reason(s) why there is a difference in the lists.
     */
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val newGame = newList?.get(newItemPosition)
        val oldGame = oldList?.get(oldItemPosition)
        val diffBundle = Bundle()

        if (newGame?.getName() != oldGame?.getName()) {
            diffBundle.putString(KEY_NAME, newGame?.getName())
        }
        if (newGame?.getPlaytime() != oldGame?.getPlaytime()) {
            diffBundle.putLong(KEY_TOTAL_PLAYTIME, newGame?.getPlaytime() ?: 0)
        }
        if (newGame?.getRecentPlaytime() != oldGame?.getRecentPlaytime()) {
            diffBundle.putLong(KEY_TOTAL_PLAYTIME, newGame?.getRecentPlaytime() ?: 0)
        }
        if (newGame?.getPercentageCompleted() != oldGame?.getPercentageCompleted()) {
            diffBundle.putFloat(KEY_PERCENTAGE, newGame?.getPercentageCompleted() ?: 0F)
        }
        if (newGame?.getAmountOfAchievements() != oldGame?.getAmountOfAchievements()) {
            diffBundle.putBoolean(KEY_ACHIEVEMENTS_UPDATED, true)
        }
        if (newGame?.getPrimaryColor() != oldGame?.getPrimaryColor()) {
            diffBundle.putInt(KEY_ACHIEVEMENTS_UPDATED, newGame?.getPrimaryColor() ?: 0)
        }
        return if (diffBundle.size() == 0) null else diffBundle
    }
}