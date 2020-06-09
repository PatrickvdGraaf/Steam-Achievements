package com.crepetete.steamachievements.presentation.activity.splash

import android.os.Bundle
import androidx.lifecycle.Observer
import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.presentation.activity.BaseActivity
import com.crepetete.steamachievements.presentation.activity.login.AuthViewModel
import com.crepetete.steamachievements.presentation.activity.login.LoginActivity
import com.crepetete.steamachievements.presentation.activity.main.MainActivity
import org.koin.android.viewmodel.ext.android.viewModel

class SplashScreenActivity : BaseActivity() {

    private val viewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set listeners
        viewModel.currentPlayerId.observe(this, Observer { id ->
            if (id == Player.INVALID_ID) {
                startActivity(LoginActivity.getInstance(this))
                finish()
            } else if (!id.isNullOrBlank()) {
                startActivity(MainActivity.getInstance(this, id))
                finish()
            }
        })
    }
}
