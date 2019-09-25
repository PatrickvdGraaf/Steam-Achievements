package com.crepetete.steamachievements.ui.fragment.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.activity.login.AuthViewModel
import javax.inject.Inject

class ProfileFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var profileViewModel: AuthViewModel

    private lateinit var imageViewProfile: ImageView
    private lateinit var textViewPersona: TextView

    companion object {
        const val TAG = "PROFILE_FRAGMENT"
        private const val KEY_PLAYER_ID = "KEY_PLAYER_ID"

        fun getInstance(playerId: String): Fragment {
            return ProfileFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_PLAYER_ID, playerId)
                }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AuthViewModel::class.java)

        // Set observers
        profileViewModel.currentPlayer.observe(this, Observer { player ->
            if (player != null) {
                textViewPersona.text = player.persona

                Glide.with(requireContext())
                    .load(player.avatarFullUrl)
                    .into(imageViewProfile)
            }
        })
    }
}
