package com.crepetete.steamachievements.ui.activity.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity<LoginPresenter>(), LoginView {
    companion object {
        fun getInstance(context: Context): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        presenter.onViewCreated(webView)
    }

    /**
     * Displays the loading indicator of the view
     */
    override fun showLoading() {
        progress.visibility = View.VISIBLE
    }

    /**
     * Hides the loading indicator of the view
     */
    override fun hideLoading() {
        progress.visibility = View.GONE
    }

    override fun onDestroy() {
        presenter.onViewDestroyed()
        super.onDestroy()
    }

    /**
     * Instantiates the presenter the Activity is based on.
     */
    override fun instantiatePresenter(): LoginPresenter {
        return LoginPresenter(this)
    }

    override fun finishActivity() {
        finish()
    }
}