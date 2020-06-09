package com.crepetete.steamachievements.presentation.common.adapter.callback

import com.crepetete.steamachievements.domain.model.Game

/**
 *
 * Used when you want to listen for the event when an Adapter has finished generating a background
 * color for a [Game] list item.
 *
 * This is currently used to update the [Game] object in the Adapters items list, saving the color.
 * When a list item is clicked, the color value is used in the GameActivity for coloring the
 * navigation bar without having to generate the color again.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 01 Dec, 2019; 11:57.
 */
interface ColorListener {
    fun onPrimaryGameColorCreated(game: Game, rgb: Int)
}