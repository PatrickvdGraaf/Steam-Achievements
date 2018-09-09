package com.crepetete.steamachievements.base

import android.content.Context
import android.widget.Toast
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

/**
 * Activity all Activity classes of rosso must extend. It provides required methods and presenter
 * instantiation and calls.
 * @param P the type of the presenter the Activity is based on
 */
abstract class BaseActivity<P : BasePresenter<BaseView>> : BaseView, DaggerAppCompatActivity() {
    @Inject
    lateinit var presenter: P

    override fun getContext(): Context {
        return this
    }

    /**
     * Displays an error in the view
     * @param error the error to display in the view
     */
    override fun showError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}