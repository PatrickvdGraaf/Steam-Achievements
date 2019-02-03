package com.crepetete.steamachievements.ui.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
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

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.run {
            putString(INTENT_USER_ID, userId)
        }
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState)
    }
}