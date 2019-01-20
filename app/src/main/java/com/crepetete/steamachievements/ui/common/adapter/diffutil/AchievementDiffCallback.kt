package com.crepetete.steamachievements.ui.common.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.crepetete.steamachievements.vo.Achievement

/**
 * Created at 20 January, 2019.
 */
class AchievementDiffCallback(
    private val oldList: List<Achievement>?,
    private val newList: List<Achievement>?
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList?.size ?: 0

    override fun getNewListSize() = newList?.size ?: 0

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = newList
        ?.get(newItemPosition)?.appId == oldList?.get(oldItemPosition)?.appId
        && newList?.get(newItemPosition)?.name == oldList?.get(oldItemPosition)?.name

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = newList
        ?.get(newItemPosition)?.equals(oldList?.get(oldItemPosition)) ?: false
}