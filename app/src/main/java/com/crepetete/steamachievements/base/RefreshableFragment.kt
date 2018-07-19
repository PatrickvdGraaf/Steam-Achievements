package com.crepetete.steamachievements.base

abstract class RefreshableFragment<P : BasePresenter<BaseView>> : BaseFragment<P>() {
    abstract fun refresh()
}