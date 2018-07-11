package com.crepetete.steamachievements.ui.activity.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.crepetete.steamachievements.BuildConfig
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.user.UserRepository
import com.crepetete.steamachievements.ui.activity.main.MainActivity
import com.crepetete.steamachievements.utils.TEST_USER_ID
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class LoginPresenter(private val loginView: LoginView) : BasePresenter<LoginView>(loginView) {

    private var disposable: CompositeDisposable = CompositeDisposable()

    @Inject
    lateinit var userRepository: UserRepository

    private val realm = loginView.getContext().getString(R.string.app_name)

    fun onViewCreated(webView: WebView) {
        view.showLoading()

        // Check if the user is already authenticated.
        val userId = userRepository.getUserId()

        when {
            userId != userRepository.getInvalidId() -> openMainActivity(userId)
            BuildConfig.DEBUG -> {
                userRepository.putUserId(TEST_USER_ID)
                openMainActivity(TEST_USER_ID)
            }
            else -> buildWebView(webView)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun buildWebView(webView: WebView) {
        loginView.showLoading()

        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val uri = Uri.parse(url)
                if (uri.authority == realm.toLowerCase()) {
                    // That means that authentication is finished and the url contains user's id.
                    webView.stopLoading()
                    // Extracts user id.
                    val userAccountUrl = Uri.parse(
                            uri.getQueryParameter("openid.identity"))
                    val userId = userAccountUrl.lastPathSegment

                    if (!userId.isNullOrBlank()) {
                        // Save the new Id
                        userRepository.putUserId(userId)
                        openMainActivity(userId)
                    }
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                         error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                Timber.e(error?.toString())
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Hide loading after first page is finished.
                loginView.hideLoading()
            }
        }

        webView.loadUrl(("https://steamcommunity.com/openid/login?" +
                "openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.identity=http://specs.openid.net/auth/2.0/identifier_select&" +
                "openid.mode=checkid_setup&" +
                "openid.ns=http://specs.openid.net/auth/2.0&" +
                "openid.realm=https://" + realm + "&" +
                "openid.return_to=https://" + realm + "/signin/"))
    }

    private fun openMainActivity(userId: String) {
        loginView.hideLoading()

        val context = view.getContext()
        view.finishActivity()
        context.startActivity(MainActivity.getInstance(context, userId))
    }

    override fun onViewDestroyed() {
        disposable.dispose()
    }
}