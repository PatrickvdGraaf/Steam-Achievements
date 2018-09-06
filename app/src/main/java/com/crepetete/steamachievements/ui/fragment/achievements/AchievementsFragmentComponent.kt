package com.crepetete.steamachievements.ui.fragment.achievements

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent(modules = [(AchievementsFragmentModule::class)])
interface AchievementsFragmentComponent : AndroidInjector<AchievementsFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<AchievementsFragment>()
}