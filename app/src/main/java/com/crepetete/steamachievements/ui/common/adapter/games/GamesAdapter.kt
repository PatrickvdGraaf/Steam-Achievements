package com.crepetete.steamachievements.ui.common.adapter.games

import androidx.lifecycle.LifecycleObserver
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.data.repository.game.GameRepository
import com.crepetete.steamachievements.databinding.GameItemBinding
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.ui.common.adapter.DataBoundListAdapter
import com.crepetete.steamachievements.utils.*

/**
 * Adapter that shows Games in a (vertical) List.
 */
class GamesAdapter(appExecutors: AppExecutors,
                   private val dataBindingComponent: DataBindingComponent,
                   private val gameRepository: GameRepository,
                   private val gameClickCallback: ((Game, ImageView) -> Unit)?)
    : DataBoundListAdapter<Game, GameItemBinding>(
        appExecutors,
        object : DiffUtil.ItemCallback<Game>() {
            override fun areItemsTheSame(oldItem: Game,
                                         newItem: Game): Boolean {
                return oldItem.appId == oldItem.appId
                        && oldItem.name == oldItem.name
            }

            override fun areContentsTheSame(oldGame: Game,
                                            newGame: Game): Boolean {
                return oldGame.name == newGame.name
                        && oldGame.playTime == newGame.playTime
                        && oldGame.recentPlayTime == newGame.recentPlayTime
                        && oldGame.iconUrl == newGame.iconUrl
                        && oldGame.logoUrl == newGame.logoUrl
                        && oldGame.getAmountOfAchievements() == newGame.getAmountOfAchievements()
                        && oldGame.getAchievedAchievements() == newGame.getAchievedAchievements()
            }
        }), LifecycleObserver {

    private var sortMethod = SortingType.PLAYTIME

    override fun createBinding(parent: ViewGroup): GameItemBinding {
        val binding = DataBindingUtil.inflate<GameItemBinding>(LayoutInflater.from(parent.context),
                R.layout.game_item, parent, false, dataBindingComponent)
        binding.root.setOnClickListener { _ ->
            binding.game?.let {
                gameClickCallback?.invoke(it, binding.gameBanner)
            }
        }

        return binding
    }

    override fun bind(binding: GameItemBinding, item: Game) {
        binding.game = item
        val view = binding.root

        binding.totalPlayedTextView.text = item.getTotalPlayTimeString(view.context)

        if (item.recentPlayTime > 0) {
            binding.recentlyPlayedTextView.text = item.getRecentPlaytimeString(view.context)
            binding.recentlyPlayedTextView.visibility = View.VISIBLE
        } else {
            binding.recentlyPlayedTextView.visibility = View.INVISIBLE
        }

//            achievementsRepository.getAchievedStatusForAchievementsForGame(game.appId, item.achievements)

        if (item.hasAchievements()) {
            binding.achievementsTextView.visibility = View.VISIBLE
            val percentage = item.getPercentageCompleted().toInt()
            if (percentage > 0 && binding.progressBar.progress == 0) {
                binding.progressBar.animateToPercentage(percentage)
            } else {
                binding.progressBar.progress = percentage
            }
            binding.achievementsTextView.text = item.getAchievementsText()
            binding.achievementsTextView.setCompletedFlag(item.isCompleted())
        } else if (!item.achievementsWereAdded()) {
            binding.achievementsTextView.visibility = View.VISIBLE
            val context = binding.content.context
            if (context != null) {
                binding.achievementsTextView.text = context.getString(R.string.msg_achievements_loading)
            }
        } else {
            binding.achievementsTextView.visibility = View.GONE
            binding.progressBar.progress = 0
        }

        Glide.with(view.context)
                .load(item.getFullLogoUrl())
                .into(object : SimpleTarget<Drawable>() {
                    /**
                     * The method that will be called when the resource load has finished.
                     *
                     * @param resource the loaded resource.
                     */
                    override fun onResourceReady(resource: Drawable,
                                                 transition: Transition<in Drawable>?) {
                        binding.gameBanner.setImageDrawable(resource)
                        if (item.colorPrimaryDark == 0 && resource is BitmapDrawable) {
                            animateBackground(item, binding.content, resource.bitmap)
                        } else {
                            binding.content.setBackgroundColor(item.colorPrimaryDark)
                        }
                    }
                })
    }

    fun submitList(list: List<Game>?, sortingType: SortingType = sortMethod) {
        if (list != null) {
            sortMethod = sortingType
            val sortedGames = sortWithAchievements(list)
            super.submitList(sortedGames)
        }
    }

    private fun sort(list: List<Game>?): List<Game> {
        if (list == null) {
            return listOf()
        }

        return when (sortMethod) {
            SortingType.PLAYTIME -> {
                list.sortByPlaytime()
            }
            SortingType.NAME -> {
                list.sortByName()
            }
            SortingType.COMPLETION -> {
                list.sortByCompletion()
            }
        }
    }

    private fun sortWithAchievements(list: List<Game>): List<Game> {
        return when (sortMethod) {
            SortingType.PLAYTIME -> {
                list.sortByPlaytime()
            }
            SortingType.NAME -> {
                list.sortByName()
            }
            SortingType.COMPLETION -> {
                list.sortByCompletion()
            }
        }
    }

    private fun animateBackground(game: Game, view: View, bitmap: Bitmap) {
        Palette.from(bitmap).generate {
            val vibrantRgb = it?.darkVibrantSwatch?.rgb
            val mutedRgb = it?.darkMutedSwatch?.rgb

            if (mutedRgb != null) {
                view.setBackgroundColorAnimated(
                        ContextCompat.getColor(view.context,
                                R.color.colorGameViewHolderTitleBackground), mutedRgb)
                game.colorPrimaryDark = mutedRgb
                gameRepository.update(game)
            } else if (vibrantRgb != null) {
                view.setBackgroundColorAnimated(
                        ContextCompat.getColor(view.context,
                                R.color.colorGameViewHolderTitleBackground), vibrantRgb)

                game.colorPrimaryDark = vibrantRgb
                gameRepository.update(game)
            }
        }
    }
}