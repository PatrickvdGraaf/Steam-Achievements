package com.crepetete.steamachievements.ui.fragment.profile

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent(modules = [(ProfileFragmentModule::class)])
interface ProfileFragmentComponent : AndroidInjector<ProfileFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ProfileFragment>()
}