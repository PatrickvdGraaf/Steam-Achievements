package com.crepetete.steamachievements.ui.activity.game.fragment.achievement.pager

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class TransparentPagerActivityFragmentProvider {
    @Module
    class AchievementPagerModule

    @ContributesAndroidInjector
    abstract fun provideAchievementPagerFragmentModule(): AchievementPagerFragment
}