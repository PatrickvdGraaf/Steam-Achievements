package com.crepetete.steamachievements.ui.activity.splash

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crepetete.steamachievements.ui.activity.BaseActivity
import com.crepetete.steamachievements.ui.activity.login.AuthViewModel
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import com.crepetete.steamachievements.ui.activity.main.MainActivity
import dagger.android.AndroidInjection
import javax.inject.Inject

class SplashScreenActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[AuthViewModel::class.java]

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
