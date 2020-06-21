package com.crepetete.steamachievements.presentation.activity.image

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.crepetete.steamachievements.R
import kotlinx.android.synthetic.main.activity_image_full_screen.*

class ImageFullScreenActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL"
        fun getIntent(context: Context, imageUrl: String): Intent {
            return Intent(context, ImageFullScreenActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URL, imageUrl)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_full_screen)

        intent.extras?.getString(EXTRA_IMAGE_URL)?.let { url ->
            Glide.with(this)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }
}
