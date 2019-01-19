package com.crepetete.steamachievements.ui.common.enums

/**
 *
 * Created by Patrick van de Graaf on 7/20/2018.
 *
 */
enum class AchievSortingMethod (val value: Int, val description: String) {
    ACHIEVED(1, "Achieved"),
    NOT_ACHIEVED(2, "Not Achieved"),
    RARITY(2, "Global completion rate")
}