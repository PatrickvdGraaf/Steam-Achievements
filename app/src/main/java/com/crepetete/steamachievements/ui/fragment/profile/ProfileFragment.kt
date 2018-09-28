package com.crepetete.steamachievements.ui.fragment.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseFragment
import com.crepetete.steamachievements.model.Player
import com.crepetete.steamachievements.ui.common.helper.LoadingIndicator

class ProfileFragment : BaseFragment<ProfilePresenter>(), ProfileView {
    private lateinit var imageViewProfile: ImageView
    private lateinit var textViewPersona: TextView

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
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        imageViewProfile = view.findViewById(R.id.imageview_profile)
        textViewPersona = view.findViewById(R.id.persona)
        return view
    }

    override fun onPlayerLoaded(player: Player) {
        textViewPersona.text = player.persona

        Glide.with(context)
                .load(player.avatarFullUrl)
                .into(imageViewProfile)
    }
}
