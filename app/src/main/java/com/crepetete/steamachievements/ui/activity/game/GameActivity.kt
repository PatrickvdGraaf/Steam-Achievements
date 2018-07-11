package com.crepetete.steamachievements.ui.activity.game

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.graphics.Palette
import android.support.v7.widget.Toolbar
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseActivity
import com.crepetete.steamachievements.model.Game

private const val INTENT_GAME_ID = "gameId"
fun Context.startGameActivity(appId: String) {
    startActivity(Intent(this, GameActivity::class.java).apply {
        putExtra(INTENT_GAME_ID, appId)
    })
}

class GameActivity : BaseActivity<GamePresenter>(), GameView {
    private val banner: ImageView by lazy { findViewById<ImageView>(R.id.banner) }
    private val scrollView: NestedScrollView by lazy { findViewById<NestedScrollView>(R.id.scrollView) }
    private val toolBar: Toolbar by lazy { findViewById<Toolbar>(R.id.main_toolbar) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setSupportActionBar(toolBar)

        presenter.onViewCreated()
    }

    private fun setTranslucentStatusBar(color: Int = ContextCompat.getColor(window.context,
            R.color.statusbar_translucent)) {
        val sdkInt = Build.VERSION.SDK_INT
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentStatusBarLollipop(color)
        } else if (sdkInt >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatusBarKiKat(window)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setTranslucentStatusBarLollipop(color: Int = ContextCompat.getColor(window.context,
            R.color.statusbar_translucent)) {
        window.statusBarColor = color
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun setTranslucentStatusBarKiKat(window: Window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    override fun onDestroy() {
        presenter.onViewDestroyed()
        super.onDestroy()
    }

    override fun setGameInfo(game: Game) {
        setTranslucentStatusBar()
        Glide.with(this).load(game.getFullLogoUrl()).into(
                object : SimpleTarget<Drawable>() {
                    /**
                     * The method that will be called when the resource load has finished.
                     *
                     * @param resource the loaded resource.
                     */
                    override fun onResourceReady(resource: Drawable,
                                                 transition: Transition<in Drawable>?) {
                        banner.setImageDrawable(resource)
                        if (resource is BitmapDrawable) {

                            Palette.from(resource.bitmap).generate {
                                val swatch = it.vibrantSwatch
                                val darkSwatch = it.darkMutedSwatch

                                if (swatch?.rgb != null) {
                                    toolBar.setBackgroundColor(swatch.rgb)
                                }
                                if (darkSwatch?.rgb != null) {
                                    setTranslucentStatusBar(darkSwatch.rgb)
                                }
                            }
                        }
                    }
                })
        toolBar.title = game.name
        title = game.name
    }

    /**
     * Displays the loading indicator of the view
     */
    override fun showLoading() {

    }

    /**
     * Hides the loading indicator of the view
     */
    override fun hideLoading() {

    }

    /**
     * Instantiates the presenter the Activity is based on.
     */
    override fun instantiatePresenter(): GamePresenter {
        return GamePresenter(this, intent.getStringExtra(INTENT_GAME_ID))
    }
}