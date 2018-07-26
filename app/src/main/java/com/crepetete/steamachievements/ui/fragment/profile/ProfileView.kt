package com.crepetete.steamachievements.ui.fragment.profile

import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.model.Player

interface ProfileView : BaseView {
    fun onPlayerLoaded(player: Player)
}