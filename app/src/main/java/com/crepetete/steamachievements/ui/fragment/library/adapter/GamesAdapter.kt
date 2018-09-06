package com.crepetete.steamachievements.ui.fragment.library.adapter

import android.support.annotation.IntDef
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.crepetete.steamachievements.R
import com.crepetete.steamachievements.base.BaseView
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.utils.sortByCompletion
import com.crepetete.steamachievements.utils.sortByName
import com.crepetete.steamachievements.utils.sortByPlaytime
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class GamesAdapter(private val baseView: BaseView,
                   private val listener: Listener? = null) : RecyclerView.Adapter<GameViewHolder>() {
    private var allGames = listOf<Game>()
    private var displayedGames = listOf<Game>()

    @SortingType
    private var sortMethod = PLAYTIME

    private var searchQuery = ""

    companion object {
        @IntDef(PLAYTIME, NAME, COMPLETION)
        @Retention(AnnotationRetention.SOURCE)
        annotation class SortingType

        const val PLAYTIME = 0
        const val NAME = 1
        const val COMPLETION = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val inflater = LayoutInflater.from(baseView.getContext())
        val view = inflater.inflate(R.layout.list_game, parent, false)
        return GameViewHolder(baseView, view, listener)
    }

    private fun getListData(): List<Game> {
        return displayedGames
    }

    override fun getItemCount() = getListData().count()

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = getListData()[position]
        holder.bind(game, listener)

        updateAchievementsForGame(game.appId)
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query

        displayedGames = if (!searchQuery.isBlank()) {
            displayedGames.filter {
                it.name.toLowerCase().contains(searchQuery.toLowerCase())
            }
        } else {
            allGames
        }

        notifyDataSetChanged()
    }

    fun updateGames(updatedGames: List<Game>) {
        allGames = updatedGames

        sort(list = updatedGames)
    }

    private fun updateAchievementsForGame(appId: String) {
//        listener?.updateAchievementsForGame(appId)
    }

    private fun getDiff(oldGames: List<Game>, newGames: List<Game>): Single<DiffUtil.DiffResult> {
        val diffCallback = GameDiffCallback(oldGames, newGames)
        return Single.fromCallable { DiffUtil.calculateDiff(diffCallback) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun sort(@SortingType type: Int) {
        sortMethod = type
        sort()
    }

    private fun sort(@SortingType type: Int = sortMethod, list: List<Game> = getListData()) {
        var sortedGames = listOf<Game>()
        when (type) {
            PLAYTIME -> {
                sortedGames = list.sortByPlaytime()
                allGames = allGames.sortByPlaytime()
            }
            NAME -> {
                sortedGames = list.sortByName()
                allGames = allGames.sortByName()
            }
            COMPLETION -> {
                sortedGames = list.sortByCompletion()
                allGames = allGames.sortByCompletion()
            }
        }

        if (listener != null) {
            listener.addDisposable(getDiff(getListData(), sortedGames)
                    .subscribe({
                        displayedGames = sortedGames
                        it.dispatchUpdatesTo(this)
                    }, {
                        Timber.e(it)
                        displayedGames = sortedGames
                        notifyDataSetChanged()
                    }))
        } else {
            displayedGames = sortedGames
            notifyDataSetChanged()
        }
        notifyDataSetChanged()
    }

    interface Listener {
        fun onGameSelected(game: Game)
        fun updateGame(game: Game)
        fun updateAchievementsForGame(appId: String)
        fun addDisposable(task: Disposable)
    }
}