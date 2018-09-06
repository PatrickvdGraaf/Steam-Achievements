package com.crepetete.steamachievements.ui.fragment.library

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent(modules = [(LibraryFragmentModule::class)])
interface LibraryFragmentComponent : AndroidInjector<LibraryFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<LibraryFragment>()
}