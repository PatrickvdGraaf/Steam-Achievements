package com.crepetete.steamachievements.ui.activity

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.activity.login.LoginActivity

/**
 *
 * Base superclass for activities in this project.
 *
 * Checks if there is a User ID present in all activities, and redirects to the [LoginActivity] if there is none.
 *
 * @author: Patrick van de Graaf.
 * @date: Sun 03 Feb, 2019; 22:51.
 */
open class BaseActivity : AppCompatActivity(), Injectable {

    companion object {
        const val INTENT_USER_ID = "user_id"
        private const val INVALID_ID = "-1"
    }

    var userId: String = INVALID_ID

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        /* Check if there is a userId property in the arguments and go to login if there is none. */
        val restoredId = savedInstanceState?.getString(INTENT_USER_ID, INVALID_ID)
        if (restoredId == INVALID_ID) {
            startActivity(LoginActivity.getInstance(this))
            return
        } else {
            userId = intent.getStringExtra(INTENT_USER_ID)
        }
    }

    protected fun setTranslucentStatusBar(color: Int = ContextCompat.getColor(window.context, R.color.statusbar_translucent)) {
        val sdkInt = Build.VERSION.SDK_INT
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        } else if (sdkInt >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            putString(INTENT_USER_ID, userId)
        }
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState)
    }
}