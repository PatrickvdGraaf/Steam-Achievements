package com.crepetete.steamachievements.ui.base

import android.content.Context
import androidx.annotation.StringRes

/**
 * Base view any view must implement.
 */
interface BaseView {
    /**
     * @return the context in which the application is running
     */
    fun getContext(): Context

    /**
     * Displays an error in the view
     * @param error the error to display in the view
     */
    fun showError(error: String)

    /**
     * Displays an error in the view
     * @param errorResId the resource id of the error to display in the view
     */
    fun showError(@StringRes errorResId: Int) {
        this.showError(getContext().getString(errorResId))
    }

    /**
     * Displays the loading indicator of the view
     */
    fun showLoading()

    /**
     * Hides the loading indicator of the view
     */
    fun hideLoading()
}