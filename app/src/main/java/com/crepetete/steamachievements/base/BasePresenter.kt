package com.crepetete.steamachievements.base

import android.widget.Toast
import com.crepetete.steamachievements.BuildConfig
import io.reactivex.disposables.CompositeDisposable

abstract class BasePresenter<out V : BaseView>(protected val view: V) {
    protected var disposable: CompositeDisposable = CompositeDisposable()
    /**
     * This method may be called when the presenter view is created.
     */
    open fun onViewCreated() {}

    /**
     * This method may be called when the presenter view is destroyed.
     */
    open fun onViewDestroyed() {
        disposable.dispose()
    }

    /**
     * If current build is a debug build, shows a short Toast message.
     */
    // TODO delete when debugging is done.
    protected fun showDebugToast(message: String) {
        if (BuildConfig.DEBUG){
            Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}