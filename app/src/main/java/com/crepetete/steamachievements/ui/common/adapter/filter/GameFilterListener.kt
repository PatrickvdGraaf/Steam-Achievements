package com.crepetete.steamachievements.ui.common.adapter.filter

import com.crepetete.steamachievements.vo.GameWithAchievements

/**
 *
 * TODO add class summary.
 *
 * @author: Patrick van de Graaf.
 * @date: Fri 08 Feb, 2019; 19:12.
 */
interface GameFilterListener {
    fun updateFilteredData(data: List<GameWithAchievements>)
}