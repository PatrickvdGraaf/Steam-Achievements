package com.crepetete.steamachievements.ui.fragment.achievement.pager

import android.graphics.Bitmap
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
import com.crepetete.steamachievements.ui.common.view.ValueWithLabelTextView
import com.crepetete.steamachievements.util.extensions.setBackgroundColorAnimated
import com.crepetete.steamachievements.util.glide.GlideApp
import com.crepetete.steamachievements.vo.Achievement
import kotlinx.android.synthetic.main.fragment_achievement_pager.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * ViewPager Fragment that shows a Dialog-like view for an [Achievement].
 */
class AchievementPagerFragment : Fragment(), Injectable {

    companion object {
        private const val INTENT_KEY_ACHIEVEMENT = "INTENT_KEY_ACHIEVEMENT"

        fun getInstance(achievement: Achievement): AchievementPagerFragment {
            val fragment = AchievementPagerFragment()
            val bundle = Bundle()
            bundle.putParcelable(INTENT_KEY_ACHIEVEMENT, achievement)
            fragment.arguments = bundle
            return fragment
        }
    }

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
        val view = inflater.inflate(R.layout.fragment_achievement_pager, container, false)
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

        // Get ViewModel and set observers.
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(PagerFragmentViewModel::class.java)

        viewModel.getAchievement().observe(this, Observer { achievement ->
            if (achievement != null) {
                setAchievementInfo(achievement)
            }
        })

        // Retrieve Achievement for this Fragment through
        arguments?.getParcelable<Achievement>(INTENT_KEY_ACHIEVEMENT)?.let { achievement ->
            viewModel.setAchievementInfo(achievement)
        }
    }

    /**
     * Update View Text labels and load icon with a listener that handles the background color of the cardview.
     */
    private fun setAchievementInfo(achievement: Achievement) {
        nameView.text = achievement.displayName
        dateView.text = getDateString(achievement)

        val desc = achievement.description
        descView.text = if (desc.isNullOrBlank()) {
            "Hidden"
        } else {
            desc
        }

        if (achievement.percentage >= 0f) {
            globalStatsLabel.setText("${achievement.percentage}%")
        }

        val context = context
        if (context != null) {

            pulsator.visibility = View.VISIBLE
            pulsator.start()

            GlideApp.with(context)
                .asBitmap()
                .load(if (achievement.achieved) achievement.iconUrl else achievement.iconGrayUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(e: GlideException?,
                                              model: Any?,
                                              target: Target<Bitmap>?,
                                              isFirstResource: Boolean): Boolean {
                        Timber.w(e, "Error while loading image from url: ${achievement.iconUrl}.")

                        pulsator.stop()

                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?,
                                                 model: Any?,
                                                 target: Target<Bitmap>?,
                                                 dataSource: DataSource?,
                                                 isFirstResource: Boolean): Boolean {
                        if (resource != null) {
                            Palette.from(resource).generate { palette ->
                                cardView.setBackgroundColorAnimated(
                                    ContextCompat.getColor(context, R.color.colorPrimary),
                                    palette?.darkMutedSwatch?.rgb ?: palette?.darkVibrantSwatch?.rgb)
                            }

                            pulsator.visibility = View.GONE
                            pulsator.stop()
                        }
                        return false
                    }
                }).into(iconView)
        }
    }

    private fun getDateString(achievement: Achievement): String {
        val cal = Calendar.getInstance()
        cal.time = achievement.unlockTime

        // Non achieved achievements will have an empty date object as their unlocktime, which results in the date being in 1970.
        // Check if the unlock time was after the year in which the development of the Steam platform was started.
        return if (cal.get(Calendar.YEAR) > 2002) {
            DateFormat.format("HH:mm\ndd-MM-yyyy", achievement.unlockTime).toString().replace("\n", " - ")
        } else {
            "Locked"
        }
    }
}
