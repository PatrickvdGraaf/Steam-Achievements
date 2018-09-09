package com.crepetete.steamachievements.ui.fragment.profile

import com.crepetete.steamachievements.data.repository.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class ProfileFragmentModule {
    @Binds
    abstract fun providesProfileView(profileFragment: ProfileFragment): ProfileView

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesProfilePresenter(libraryView: ProfileView,
                                     userRepository: UserRepository) = ProfilePresenter(libraryView, userRepository)
    }
}