package com.crepetete.steamachievements.ui.activity.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.activity.main.MainActivity
import com.crepetete.steamachievements.vo.Status
import kotlinx.android.synthetic.main.activity_login.*
import timber.log.Timber
import javax.inject.Inject

class LoginActivity : AppCompatActivity(), Injectable {
    companion object {
        fun getInstance(context: Context): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            return intent
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(AuthViewModel::class.java)

        // Set listeners
        viewModel.currentPlayer.observe(this, Observer {
            if (it?.status == Status.LOADING) {
                progress.visibility = View.VISIBLE
            } else {
                if (it?.data != null) {
                    startActivity(MainActivity.getInstance(this, it.data.steamId))
                    Handler().postDelayed(::finish, 1000)
                }

                progress.visibility = View.GONE
            }
        })

        setupWebView()


    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val realm = getRealm()

        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val uri = Uri.parse(url)
                if (uri.authority == realm) {
                    viewModel.parseIdFromUri(uri)
                }
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?,
                                         error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                Timber.e(error?.toString())

                webView.stopLoading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                webView.stopLoading()
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

    // TODO move this to ViewModel once a StringManager is implemented
    private fun getRealm(): String {
        return getString(R.string.app_name).toLowerCase()
    }
}