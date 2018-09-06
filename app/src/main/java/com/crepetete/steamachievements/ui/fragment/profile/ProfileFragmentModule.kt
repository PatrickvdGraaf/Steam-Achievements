package com.crepetete.steamachievements.ui.fragment.profile

import com.crepetete.steamachievements.data.repository.user.UserRepository
import dagger.Module
import dagger.Provides

@Module
class ProfileFragmentModule {
    @Provides
    fun providesProfileView(profileFragment: ProfileFragment): ProfileView {
        return profileFragment
    }

    @Provides
    fun providesProfilePresenter(libraryView: ProfileView,
                                 userRepository: UserRepository): ProfilePresenter {
        return ProfilePresenter(libraryView, userRepository)
    }
}