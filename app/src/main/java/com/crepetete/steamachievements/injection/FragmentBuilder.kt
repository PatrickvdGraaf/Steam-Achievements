package com.crepetete.steamachievements.injection

import android.support.v4.app.Fragment
import com.crepetete.steamachievements.ui.fragment.achievements.AchievementsFragment
import com.crepetete.steamachievements.ui.fragment.achievements.AchievementsFragmentComponent
import com.crepetete.steamachievements.ui.fragment.library.LibraryFragment
import com.crepetete.steamachievements.ui.fragment.library.LibraryFragmentComponent
import com.crepetete.steamachievements.ui.fragment.profile.ProfileFragment
import com.crepetete.steamachievements.ui.fragment.profile.ProfileFragmentComponent
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module
abstract class FragmentBuilder {
    @Binds
    @IntoMap
    @FragmentKey(AchievementsFragment::class)
    internal abstract fun bindAchievementsFragment(builder: AchievementsFragmentComponent.Builder)
            : AndroidInjector.Factory<out Fragment>

    @Binds
    @IntoMap
    @FragmentKey(LibraryFragment::class)
    internal abstract fun bindLibraryFragment(builder: LibraryFragmentComponent.Builder)
            : AndroidInjector.Factory<out Fragment>

    @Binds
    @IntoMap
    @FragmentKey(ProfileFragment::class)
    internal abstract fun bindProfileFragment(builder: ProfileFragmentComponent.Builder)
            : AndroidInjector.Factory<out Fragment>
}