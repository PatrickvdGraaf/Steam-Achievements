package com.crepetete.steamachievements.presentation.fragment.achievement.pager

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.presentation.SteamAchievementsApp
import com.crepetete.steamachievements.presentation.di.Injectable
import com.crepetete.steamachievements.util.Constants
import com.crepetete.steamachievements.util.extensions.animateBackground
import kotlinx.android.synthetic.main.fragment_achievement_pager.*
import java.util.Calendar
import java.util.Date
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
    lateinit var viewModel: PagerFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_achievement_pager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getAchievement().observe(viewLifecycleOwner, Observer { achievement ->
            if (achievement != null) {
                setAchievementInfo(achievement)
            }
        })

        // Retrieve Achievement for this Fragment through arguments.
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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (activity!!.application as SteamAchievementsApp).appComponent.inject(this)
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

        loadIcon(
            if (achievement.achieved) achievement.iconUrl ?: "" else achievement.iconGrayUrl ?: ""
        )
    }

    private fun loadIcon(url: String) {
        pulsator.visibility = View.VISIBLE
        pulsator.start()

        Glide.with(achievement_icon_imageview.context)
            .asBitmap()
            .load(url)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    pulsator.hide()

                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let { bitmap ->
                        Palette.from(bitmap).generate { palette ->
                            val defaultColor = ContextCompat.getColor(
                                requireContext(),
                                R.color.colorPrimary
                            )
                            val newColor = palette?.let { p ->
                                p.darkMutedSwatch ?: p.darkVibrantSwatch
                            }

                            achievement_cardview.animateBackground(defaultColor, newColor?.rgb)
                        }
                    }

                    pulsator.hide()

                    return false
                }
            })
            .into(achievement_icon_imageview)
    }

    /**
     * Non achieved achievements will have an empty date object as their unlockTime, which
     * results in the date being in 1970. This isn't useful to show in views.
     * So we check if the unlock date is after the official Steam Release date to determine
     * if the date is valid.
     */
    private fun getDateString(achievement: Achievement): String {
        val calendarUnlock = Calendar.getInstance()
        calendarUnlock.time = achievement.unlockTime ?: Date()

        val calendarSteam = Constants.steamReleaseCalendar

        return if (calendarUnlock.get(Calendar.YEAR) > calendarSteam.get(Calendar.YEAR)) {
            DateFormat.format(
                "HH:mm\ndd-MM-yyyy",
                achievement.unlockTime
            ).toString()
                .replace("\n", " - ")
        } else {
            "Locked"
        }
    }
}
