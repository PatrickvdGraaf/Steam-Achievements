package com.crepetete.steamachievements.ui.activity.splash

import android.os.Bundle
import androidx.lifecycle.Observer
import com.crepetete.steamachievements.SteamAchievementsApp
import com.crepetete.steamachievements.ui.activity.BaseActivity
import com.crepetete.steamachievements.ui.activity.login.AuthViewModel
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.activity.main.MainActivity
import javax.inject.Inject

class SplashScreenActivity : BaseActivity() {

    @Inject
    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as SteamAchievementsApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)

        // Set listeners
        viewModel.currentPlayerId.observe(this, Observer { id ->
            if (id == viewModel.invalidUserId) {
                startActivity(LoginActivity.getInstance(this))
                finish()
            } else if (!id.isNullOrBlank()) {
                startActivity(MainActivity.getInstance(this, id))
                finish()
            }
        })
    }
}
