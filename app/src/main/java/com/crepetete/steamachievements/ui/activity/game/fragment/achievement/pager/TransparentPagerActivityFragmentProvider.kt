package com.crepetete.steamachievements.ui.activity.game.fragment.achievement.pager

import dagger.Module

@Suppress("unused")
@Module
abstract class TransparentPagerActivityFragmentProvider {
    @Module
    class AchievementPagerModule

    abstract fun provideAchievementPagerFragmentModule(): AchievementPagerFragment
}