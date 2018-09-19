package com.crepetete.steamachievements.ui.common.adapter.games

import android.arch.lifecycle.LifecycleObserver
import android.databinding.DataBindingComponent
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.crepetete.steamachievements.AppExecutors
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.data.database.model.GameWithAchievements
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
    : DataBoundListAdapter<GameWithAchievements, GameItemBinding>(
        appExecutors,
        object : DiffUtil.ItemCallback<GameWithAchievements>() {
            override fun areItemsTheSame(oldItem: GameWithAchievements,
                                         newItem: GameWithAchievements): Boolean {
                return oldItem.game?.appId == oldItem.game?.appId
                        && oldItem.game?.name == oldItem.game?.name
            }

            override fun areContentsTheSame(oldGame: GameWithAchievements,
                                            newGame: GameWithAchievements): Boolean {
                return oldGame.game?.name == newGame.game?.name
                        && oldGame.game?.playTime == newGame.game?.playTime
                        && oldGame.game?.recentPlayTime == newGame.game?.recentPlayTime
                        && oldGame.game?.iconUrl == newGame.game?.iconUrl
                        && oldGame.game?.logoUrl == newGame.game?.logoUrl
                        && oldGame.achievements.size == newGame.achievements.size
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

    override fun bind(binding: GameItemBinding, item: GameWithAchievements) {
        val game = item.game
        if (game != null) {
            binding.game = game
            val view = binding.root

            binding.totalPlayedTextView.text = game.getTotalPlayTimeString(view.context)

            if (game.recentPlayTime > 0) {
                binding.recentlyPlayedTextView.text = game.getRecentPlaytimeString(view.context)
                binding.recentlyPlayedTextView.visibility = View.VISIBLE
            } else {
                binding.recentlyPlayedTextView.visibility = View.INVISIBLE
            }

//            achievementsRepository.getAchievedStatusForAchievementsForGame(game.appId, item.achievements)

            if (game.hasAchievements()) {
                binding.achievementsTextView.visibility = View.VISIBLE
                val percentage = game.getPercentageCompleted().toInt()
                if (percentage > 0 && binding.progressBar.progress == 0) {
                    binding.progressBar.animateToPercentage(percentage)
                } else {
                    binding.progressBar.progress = percentage
                }
                binding.achievementsTextView.text = game.getAchievementsText()
                binding.achievementsTextView.setCompletedFlag(game.isCompleted())
            } else if (!game.achievementsWereAdded()) {
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
                    .load(game.getFullLogoUrl())
                    .into(object : SimpleTarget<Drawable>() {
                        /**
                         * The method that will be called when the resource load has finished.
                         *
                         * @param resource the loaded resource.
                         */
                        override fun onResourceReady(resource: Drawable,
                                                     transition: Transition<in Drawable>?) {
                            binding.gameBanner.setImageDrawable(resource)
                            if (game.colorPrimaryDark == 0 && resource is BitmapDrawable) {
                                animateBackground(game, binding.content, resource.bitmap)
                            } else {
                                binding.content.setBackgroundColor(game.colorPrimaryDark)
                            }
                        }
                    })
        }
    }

    fun submitList(list: List<GameWithAchievements>?, sortingType: SortingType = sortMethod) {
        if (list != null) {
            sortMethod = sortingType
            val sortedGames = sortWithAchievements(list.filter { it.game != null })
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

    private fun sortWithAchievements(list: List<GameWithAchievements>): List<GameWithAchievements> {
        return when (sortMethod) {
            SortingType.PLAYTIME -> {
                list.sortPlaytime()
            }
            SortingType.NAME -> {
                list.sortName()
            }
            SortingType.COMPLETION -> {
                list.sortCompletion()
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