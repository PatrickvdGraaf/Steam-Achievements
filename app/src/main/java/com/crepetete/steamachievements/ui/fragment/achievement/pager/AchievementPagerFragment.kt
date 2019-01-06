package com.crepetete.steamachievements.ui.fragment.achievement.pager

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.ui.view.component.ValueWithLabelTextView
import com.crepetete.steamachievements.util.GlideApp
import com.crepetete.steamachievements.util.extensions.setBackgroundColorAnimated
import com.crepetete.steamachievements.vo.Achievement
import javax.inject.Inject

/**
 * ViewPager Fragment that shows a Dialog-like view for an [Achievement].
 */
class AchievementPagerFragment : Fragment(), Injectable {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        dateView.text = getDateStringNoBreak(achievement)

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

    fun getDateStringNoBreak(achievement: Achievement): String {
        return getDateString(achievement).replace("\n", " - ")
    }

    fun getDateStringNoTime(achievement: Achievement): String {
        return DateFormat.format("dd-MM-yyyy", achievement.unlockTime).toString()
    }

    private fun getDateString(achievement: Achievement): String {
        return if (achievement.unlockTime != null) {
            DateFormat.format("HH:mm\ndd-MM-yyyy", achievement.unlockTime).toString()
        } else {
            "Locked"
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
