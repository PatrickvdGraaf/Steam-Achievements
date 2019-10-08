package com.crepetete.steamachievements.ui.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.activity.login.LoginActivity
import dagger.android.AndroidInjection

/**
 *
 * Base superclass for activities in this project.
 *
 * Checks if there is a User ID present in all activities, and redirects to the [LoginActivity] if there is none.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 03 Feb, 2019; 22:51.
 */
abstract class BaseActivity : AppCompatActivity(), Injectable {

    companion object {
        const val INTENT_USER_ID = "user_id"
        private const val INVALID_ID = "-1"
    }

    var userId: String = INVALID_ID

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState, persistentState)

        /* Check if there is a userId property in the arguments and go to login if there is none. */
        val restoredId = savedInstanceState?.getString(INTENT_USER_ID, INVALID_ID)
        if (restoredId == INVALID_ID) {
            startActivity(LoginActivity.getInstance(this))
            return
        } else {
            userId = intent.getStringExtra(INTENT_USER_ID) ?: INVALID_ID
        }
    }

    protected fun setTranslucentStatusBar() {
        setStatusBarColor(ContextCompat.getColor(window.context, R.color.statusbar_translucent))
    }

    private fun setStatusBarColor(@ColorInt color: Int) {
        window.statusBarColor = color
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putString(INTENT_USER_ID, userId)
        }
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState)
    }
}