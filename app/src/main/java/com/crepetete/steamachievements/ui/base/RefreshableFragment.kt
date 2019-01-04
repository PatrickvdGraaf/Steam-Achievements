package com.crepetete.steamachievements.ui.base

abstract class RefreshableFragment<P : BasePresenter<BaseView>> : BaseFragment<P>() {
    abstract fun refresh()
}