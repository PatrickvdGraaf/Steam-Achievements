package com.crepetete.steamachievements.presentation.fragment.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.presentation.activity.login.AuthViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {

    private val profileViewModel: AuthViewModel by viewModel()

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        imageViewProfile = view.findViewById(R.id.imageview_profile)
        textViewPersona = view.findViewById(R.id.persona)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set observers
        profileViewModel.currentPlayer.observe(viewLifecycleOwner, Observer { player ->
            if (player != null) {
                textViewPersona.text = player.persona

                Glide.with(requireContext())
                    .load(player.avatarFullUrl)
                    .into(imageViewProfile)
            }
        })
    }
}
