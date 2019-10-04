package com.crepetete.steamachievements.di.submodules

import com.crepetete.steamachievements.ui.activity.game.fragment.achievement.pager.AchievementPagerFragment
import com.crepetete.steamachievements.ui.activity.main.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.ui.activity.main.fragment.library.LibraryFragment
import com.crepetete.steamachievements.ui.activity.main.fragment.profile.ProfileFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun contributeAchievementsFragment(): AchievementsFragment

    @ContributesAndroidInjector
    abstract fun contributeLibraryFragment(): LibraryFragment

    @ContributesAndroidInjector
    abstract fun contributeProfileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun contributeAchievementPagerFragment(): AchievementPagerFragment
}