package com.crepetete.steamachievements.di.submodules

import com.crepetete.steamachievements.ui.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.ui.fragment.library.LibraryFragment
import com.crepetete.steamachievements.ui.fragment.profile.ProfileFragment
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