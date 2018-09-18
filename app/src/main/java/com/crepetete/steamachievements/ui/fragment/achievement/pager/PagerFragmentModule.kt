package com.crepetete.steamachievements.ui.fragment.achievement.pager

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PagerFragmentModule {
    @ContributesAndroidInjector(modules = [(AchievementPagerModule::class)])
    abstract fun provideAchievementPagerModule(): AchievementPagerFragment
}