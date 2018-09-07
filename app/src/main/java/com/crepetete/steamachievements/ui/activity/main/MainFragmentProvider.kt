package com.crepetete.steamachievements.ui.activity.main

import com.crepetete.steamachievements.ui.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.ui.fragment.achievements.AchievementsFragmentModule
import com.crepetete.steamachievements.ui.fragment.library.LibraryFragment
import com.crepetete.steamachievements.ui.fragment.library.LibraryFragmentModule
import com.crepetete.steamachievements.ui.fragment.profile.ProfileFragment
import com.crepetete.steamachievements.ui.fragment.profile.ProfileFragmentModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainFragmentProvider {
    @ContributesAndroidInjector(modules = [(ProfileFragmentModule::class)])
    abstract fun provideProfileFragmentModule(): ProfileFragment

    @ContributesAndroidInjector(modules = [(LibraryFragmentModule::class)])
    abstract fun provideLibraryFragmentModule(): LibraryFragment

    @ContributesAndroidInjector(modules = [(AchievementsFragmentModule::class)])
    abstract fun provideAchievementsFragmentModule(): AchievementsFragment
}