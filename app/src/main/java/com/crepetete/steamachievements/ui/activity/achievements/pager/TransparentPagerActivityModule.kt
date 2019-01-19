package com.crepetete.steamachievements.ui.activity.achievements.pager

import com.crepetete.steamachievements.di.FragmentBuildersModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class TransparentPagerActivityModule {
    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeTransparentPagerActivity(): TransparentPagerActivity
}