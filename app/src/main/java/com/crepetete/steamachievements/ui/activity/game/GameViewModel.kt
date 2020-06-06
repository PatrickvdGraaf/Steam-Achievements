package com.crepetete.steamachievements.ui.activity.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
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

    val news: LiveData<List<NewsItem>?> = _news

    private val sortingComparator = MutableLiveData<Order.BaseComparator<Achievement>>()

    private var index = 0

    private val sortingMethods: HashMap<Int, Order.BaseComparator<Achievement>> = hashMapOf(
        0 to Order.LatestAchievedOrder(),
        1 to Order.RarityOrder(),
        2 to Order.NotAchievedOrder()
    )

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

    fun setGame(newGame: Game) {
        _appId.value = AppId(newGame.getAppId().toString())
    }

    fun fetchNews() {
        if (_newsLoadingState.value == LiveResource.STATE_LOADING) {
            return
        }

        _appId.value?.id?.let { id ->
            uiScope.launch {
                gameRepo.getNews(id).apply {
                    _newsLiveResource = this
                    newsJob = this.job
                    bindObserver(_news, this.data)
                    bindObserver(_newsLoadingState, this.state)
                    bindObserver(_newsLoadingError, this.error)
                }
            }
        }
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