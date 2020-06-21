package com.crepetete.steamachievements.presentation.activity.login

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
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.data.helper.Resource
import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.presentation.activity.main.MainActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.Locale

class LoginActivity : AppCompatActivity() {
    companion object {
        fun getInstance(context: Context): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            return intent
        }
    }

    private val viewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set listeners
        with(viewModel) {
            currentPlayerId.observe(this@LoginActivity, Observer { id ->
                id?.let {
                    if (id != Player.INVALID_ID) {
                        startActivity(MainActivity.getInstance(this@LoginActivity, it))
                        Handler().postDelayed(::finish, 1000)
                    }
                }
            })

            idLoadingState.observe(this@LoginActivity, Observer { state ->
                when (state) {
                    Resource.STATE_LOADING -> {
                        progress.visibility = View.VISIBLE
                    }
                    Resource.STATE_SUCCESS, Resource.STATE_FAILED ->
                        progress.visibility = View.GONE
                }
            })

            idLoadingError.observe(this@LoginActivity, Observer { exception ->
                exception?.let {
                    Timber.e("Error while fetching user ID: ${exception.localizedMessage}")

                    progress.visibility = View.GONE

                    // TODO add error feedback.
                }
            })
        }

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

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Timber.e(error?.toString())

                webView.stopLoading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                webView.stopLoading()
            }
        }

        webView.loadUrl(
            ("https://steamcommunity.com/openid/login" +
                    "?openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select" +
                    "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
                    "&openid.mode=checkid_setup" +
                    "&openid.ns=http://specs.openid.net/auth/2.0" +
                    "&openid.realm=https://$realm" +
                    "&openid.return_to=https://$realm/signin/")
        )
    }

    // TODO move this to ViewModel once a StringManager is implemented
    private fun getRealm(): String {
        return getString(R.string.app_name).toLowerCase(Locale.ENGLISH)
    }
}