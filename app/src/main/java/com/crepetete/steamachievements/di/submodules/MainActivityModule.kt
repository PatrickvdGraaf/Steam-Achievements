package com.crepetete.steamachievements.di.submodules

import com.crepetete.steamachievements.ui.activity.main.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.ui.activity.main.fragment.library.LibraryFragment
import com.crepetete.steamachievements.ui.activity.main.fragment.profile.ProfileFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector
    abstract fun provideProfileFragmentModule(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun provideLibraryFragmentModule(): LibraryFragment

    @ContributesAndroidInjector
    abstract fun provideAchievementsFragmentModule(): AchievementsFragment
}