package com.crepetete.steamachievements.ui.fragment.achievement.pager


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.ui.view.component.ValueWithLabelTextView
import com.crepetete.steamachievements.utils.GlideApp
import com.crepetete.steamachievements.utils.setBackgroundColorAnimated
import dagger.android.support.DaggerFragment
import javax.inject.Inject

/**
 * ViewPager Fragment that shows a Dialog-like view for an [Achievement].
 */
class AchievementPagerFragment : DaggerFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: PagerFragmentViewModel

    private lateinit var cardView: CardView
    private lateinit var iconContent: CardView
    private lateinit var scrollView: ScrollView
    private lateinit var iconView: ImageView
    private lateinit var nameView: TextView
    private lateinit var dateView: TextView
    private lateinit var descView: TextView
    private lateinit var globalStatsLabel: ValueWithLabelTextView
    private lateinit var content: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_achievement_pager, container,
                false)
        iconView = view.findViewById(R.id.achievement_icon_imageview)
        cardView = view.findViewById(R.id.achievement_cardview)
        nameView = view.findViewById(R.id.achievement_name_textview)
        dateView = view.findViewById(R.id.achievement_date_textview)
        descView = view.findViewById(R.id.achievement_desc_textview)
        content = view.findViewById(R.id.content)
        scrollView = view.findViewById(R.id.scrollView)
        globalStatsLabel = view.findViewById(R.id.label_global_stats)
        iconContent = view.findViewById(R.id.icon_card_view)

        content.setOnClickListener {
            activity?.onBackPressed()
        }

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(PagerFragmentViewModel::class.java)

        val achievementName = arguments?.getString(INTENT_KEY_NAME)
        val achievementAppId = arguments?.getString(INTENT_KEY_APP_ID)
        if (achievementName != null && achievementAppId != null) {
            viewModel.setAchievementInfo(achievementName, achievementAppId)
        }

        viewModel.achievements.observe(this, Observer {
            if (it != null) {
                val achievement = it[0]
                setAchievementInfo(achievement)
                setIconAndColors(achievement.iconUrl)
            }
        })
    }

    private fun setAchievementInfo(achievement: Achievement) {
        nameView.text = achievement.displayName
        dateView.text = achievement.getDateStringNoBreak()

        val desc = achievement.description
        if (desc.isNullOrBlank()) {
            descView.visibility = View.GONE
        } else {
            descView.text = achievement.description
            descView.visibility = View.VISIBLE
        }

        if (achievement.percentage > 0.0) {
            globalStatsLabel.setText("${achievement.percentage}%")
        }
    }

    private fun setIconAndColors(iconUrl: String) {
        val context = context
        if (context != null) {
            GlideApp.with(context)
                    .load(iconUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?,
                                                  model: Any?,
                                                  target: Target<Drawable>?,
                                                  isFirstResource: Boolean): Boolean {
                            // TODO
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?,
                                                     model: Any?,
                                                     target: Target<Drawable>?,
                                                     dataSource: DataSource?,
                                                     isFirstResource: Boolean): Boolean {
                            if (resource != null && resource is BitmapDrawable) {
                                Palette.from(resource.bitmap).generate {
                                    val darkVibrantSwatch = it?.darkVibrantSwatch
                                    val darkMutedSwatch = it?.darkMutedSwatch

                                    if (darkMutedSwatch != null) {
                                        cardView.setCardBackgroundColor(darkMutedSwatch.rgb)
                                        iconContent.setCardBackgroundColor(darkMutedSwatch.rgb)
                                        dateView.setTextColor(darkMutedSwatch.bodyTextColor)
                                    } else if (darkVibrantSwatch != null) {
                                        cardView.setBackgroundColorAnimated(
                                                ContextCompat.getColor(context, R.color.colorPrimary),
                                                darkVibrantSwatch.rgb, 300)
                                        iconContent.setBackgroundColorAnimated(
                                                ContextCompat.getColor(context,
                                                        R.color.colorPrimaryDark),
                                                darkVibrantSwatch.rgb)
                                    }
                                }
                            }
                            return false
                        }
                    }).into(iconView)
        }
    }

    companion object {
        const val INTENT_KEY_NAME = "INTENT_KEY_NAME"
        const val INTENT_KEY_APP_ID = "INTENT_KEY_APP_ID"
    }
}
