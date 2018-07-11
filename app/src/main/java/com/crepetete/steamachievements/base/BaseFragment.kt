package com.crepetete.steamachievements.base

import android.content.Context
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.view.View
import android.widget.Toast
import com.crepetete.steamachievements.ui.activity.helper.LoadingIndicator

abstract class BaseFragment<P : BasePresenter<BaseView>> : BaseView, Fragment() {
    protected lateinit var presenter: P

    private var loadingIndicator: LoadingIndicator? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = instantiatePresenter()
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.onViewCreated()
    }

    @CallSuper
    override fun onDestroyView() {
        presenter.onViewDestroyed()
        super.onDestroyView()
    }

    fun setLoaderIndicator(indicator: LoadingIndicator) {
        loadingIndicator = indicator
    }

    override fun showLoading() {
        loadingIndicator?.showLoading()
    }

    override fun hideLoading() {
        loadingIndicator?.hideLoading()
    }

    /**
     * Instantiates the presenter the Activity is based on.
     */
    protected abstract fun instantiatePresenter(): P

    override fun getContext(): Context {
        return activity!!
    }

    /**
     * Displays an error in the view
     * @param error the error to display in the view
     */
    override fun showError(error: String) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }
}