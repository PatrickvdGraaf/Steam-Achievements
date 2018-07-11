package com.crepetete.steamachievements.ui.fragment.profile


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseFragment
import com.crepetete.steamachievements.ui.activity.helper.LoadingIndicator

class ProfileFragment : BaseFragment<ProfilePresenter>(), ProfileView {
    /**
     * Instantiates the presenter the Activity is based on.
     */
    override fun instantiatePresenter(): ProfilePresenter {
        return ProfilePresenter(this)
    }

    companion object {
        const val TAG = "PROFILE_FRAGMENT"
        private const val KEY_PLAYER_ID = "KEY_PLAYER_ID"

        fun getInstance(playerId: String, loadingIndicator: LoadingIndicator): Fragment {
            return ProfileFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_PLAYER_ID, playerId)
                }
                setLoaderIndicator(loadingIndicator)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }
}
