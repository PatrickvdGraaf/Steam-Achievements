package com.crepetete.steamachievements.ui.activity.game.fragment.achievement.pager

import android.graphics.Bitmap
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.di.Injectable
import com.crepetete.steamachievements.util.extensions.setBackgroundColorAnimated
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

    @ColorInt
    private var backgroundColor: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_achievement_pager, container, false)

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

            achievement_icon_imageview.setOnClickListener {
                if (!achievement.achieved) {
                    achievement_icon_imageview.setOnClickListener(null)
                    loadIcon(achievement.iconUrl ?: "")
                }
            }
        }

        content.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    /**
     * Update View Text labels and load icon with a listener that handles the background color of the cardview.
     */
    private fun setAchievementInfo(achievement: Achievement) {
        achievement_name_textview.text = achievement.displayName
        achievement_date_textview.text = getDateString(achievement)

        val desc = achievement.description
        achievement_desc_textview.text = if (desc.isNullOrBlank()) {
            "Hidden"
        } else {
            desc
        }

        if (achievement.percentage >= 0f) {
            label_global_stats.setText("${achievement.percentage}%")
        }

        loadIcon(if (achievement.achieved) achievement.iconUrl ?: "" else achievement.iconGrayUrl ?: "")
    }

    private fun loadIcon(url: String) {
        pulsator.visibility = View.VISIBLE
        pulsator.start()

        Glide.with(requireContext())
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?,
                                          model: Any?,
                                          target: Target<Bitmap>?,
                                          isFirstResource: Boolean): Boolean {
                    Timber.w(e, "Error while loading image from url: $url.")

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
                            val defaultColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                            val newColor = palette?.darkMutedSwatch?.rgb ?: palette?.darkVibrantSwatch?.rgb ?: defaultColor

                            achievement_cardview.setBackgroundColorAnimated(backgroundColor ?: defaultColor, newColor)

                            backgroundColor = newColor
                        }

                        pulsator.visibility = View.GONE
                        pulsator.stop()
                    }
                    return false
                }
            }).into(achievement_icon_imageview)
    }

    private fun getDateString(achievement: Achievement): String {
        val cal = Calendar.getInstance()
        cal.time = achievement.unlockTime ?: Date()

        // Non achieved achievements will have an empty date object as their unlocktime, which results in the date being in 1970.
        // Check if the unlock time was after the year in which the development of the Steam platform was started.
        return if (cal.get(Calendar.YEAR) > 2002) {
            DateFormat.format("HH:mm\ndd-MM-yyyy", achievement.unlockTime).toString().replace("\n", " - ")
        } else {
            "Locked"
        }
    }
}
