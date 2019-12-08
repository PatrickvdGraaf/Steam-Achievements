package com.crepetete.steamachievements.ui.activity.game

import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import com.crepetete.steamachievements.api.response.news.NewsItem
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.repository.resource.LiveResource
import com.crepetete.steamachievements.repository.resource.ResourceState
import com.crepetete.steamachievements.ui.common.adapter.sorting.AchievementSortedListImpl
import com.crepetete.steamachievements.ui.common.adapter.sorting.Order
import com.crepetete.steamachievements.util.extensions.bindObserver
import com.crepetete.steamachievements.util.livedata.AbsentLiveData
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

class GameViewModel @Inject constructor(
    private val gameRepo: GameRepository
) : ViewModel() {

    private val mainJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mainJob)
    private var newsJob: Job? = null

    private val _appId = MutableLiveData<AppId>()
    val appId: LiveData<AppId>
        get() = _appId

    // Game
    val game: LiveData<Game> = Transformations
        .switchMap(_appId) { id ->
            id.ifExists { appId ->
                gameRepo.getGame(appId)
            }
        }

    // News
    private var _newsLiveResource: LiveResource<List<NewsItem>>? = null
    private val _news = MediatorLiveData<List<NewsItem>?>()
    private val _newsLoadingState = MediatorLiveData<@ResourceState Int?>()
    private val _newsLoadingError = MediatorLiveData<Exception?>()

    val news: LiveData<List<NewsItem>?>
        get() {
            Transformations.map(_appId) { id ->
                if (id != null) {
                    uiScope.launch {
                        gameRepo.getNews(id.id).apply {
                            _newsLiveResource = this
                            newsJob = this.job
                            bindObserver(_news, this.data)
                            bindObserver(_newsLoadingState, this.state)
                            bindObserver(_newsLoadingError, this.error)
                        }
                    }
                }
            }
            return _news
        }

    private val sortingComparator = MutableLiveData<Order.BaseComparator<Achievement>>()

    private var index = 0

    private val sortingMethods: HashMap<Int, Order.BaseComparator<Achievement>> = hashMapOf(
        0 to Order.LatestAchievedOrder(),
        1 to Order.RarityOrder(),
        2 to Order.NotAchievedOrder()
    )

    /* Colors */
    private val vibrantColor: MutableLiveData<Palette.Swatch> = MutableLiveData()
    private val mutedColor: MutableLiveData<Palette.Swatch> = MutableLiveData()

    init {
        setAchievementSortingMethod(AchievementSortedListImpl.DEFAULT_ORDER)
    }

    /**
     * Update current sorting method for the achievements list.
     */
    fun setAchievementSortingMethod(method: Order.BaseComparator<Achievement>? = null) {
        sortingComparator.value = method ?: getNextSortingMethod()
    }

    /**
     * Increases current [index] or goes back to 0 is the index in the same
     * as the [sortingMethods] size.
     *
     * @return next sorting method based on the newly increased [index].
     */
    private fun getNextSortingMethod(): Order.BaseComparator<Achievement> {
        if (index == sortingMethods.keys.size) index = 0 else index++
        return sortingMethods[index] ?: AchievementSortedListImpl.DEFAULT_ORDER
    }

    /**
     * Getter for [sortingComparator] for observers.
     */
    fun getAchievementSortingMethod() = sortingComparator

    fun updatePalette(palette: Palette) {
        if (palette.darkMutedSwatch != null && palette.darkVibrantSwatch != null) {
            mutedColor.postValue(palette.darkMutedSwatch)
            vibrantColor.postValue(palette.darkVibrantSwatch)
        } else if (palette.darkMutedSwatch != null && palette.mutedSwatch != null) {
            mutedColor.postValue(palette.mutedSwatch)
            vibrantColor.postValue(palette.darkMutedSwatch)
        } else if (palette.darkVibrantSwatch != null && palette.mutedSwatch != null) {
            mutedColor.postValue(palette.mutedSwatch)
            vibrantColor.postValue(palette.darkVibrantSwatch)
        } else if (palette.lightVibrantSwatch != null && palette.mutedSwatch != null) {
            mutedColor.postValue(palette.mutedSwatch)
            vibrantColor.postValue(palette.lightVibrantSwatch)
        } else if (palette.lightVibrantSwatch != null && palette.lightMutedSwatch != null) {
            mutedColor.postValue(palette.lightVibrantSwatch)
            vibrantColor.postValue(palette.lightMutedSwatch)
        }
    }

    fun setAppId(appId: String) {
        _appId.value = AppId(appId)
    }

    fun setGame(newGame: Game) {
        _appId.value = AppId(newGame.getAppId().toString())
    }

    data class AppId(val id: String) {
        fun <T> ifExists(f: (String) -> LiveData<T>): LiveData<T> {
            return if (id.isBlank()) {
                AbsentLiveData.create()
            } else {
                f(id)
            }
        }
    }
}