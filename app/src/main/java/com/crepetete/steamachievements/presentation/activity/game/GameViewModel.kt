package com.crepetete.steamachievements.presentation.activity.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.data.helper.ResourceState
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.domain.usecases.news.GetNewsUseCase
import com.crepetete.steamachievements.presentation.common.adapter.sorting.AchievementSortedListImpl
import com.crepetete.steamachievements.presentation.common.adapter.sorting.Order
import com.crepetete.steamachievements.util.extensions.bindObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class GameViewModel(private val getNewsUseCase: GetNewsUseCase) : ViewModel() {

    private val mainJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mainJob)
    private var newsJob: Job? = null

    private val _appId = MutableLiveData<AppId>()
    val appId: LiveData<AppId>
        get() = _appId

    // Game
    private val gameMutable: MutableLiveData<Game?> = MutableLiveData<Game?>(null)
    val game: LiveData<Game?>
        get(): LiveData<Game?> = gameMutable

    // News
    private var _newsLiveResource: LiveResource<List<NewsItem>>? = null
    private val _newsLoadingState = MediatorLiveData<@ResourceState Int?>()
    private val _newsLoadingError = MediatorLiveData<Exception?>()

    private val _news = MediatorLiveData<List<NewsItem>?>()
    val news: LiveData<List<NewsItem>?> = _news

    // Chart
    private val _achievementsChartData = MutableLiveData<List<Achievement?>>()
    val achievementsChartData: LiveData<List<Achievement?>>
        get() = _achievementsChartData

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
        gameMutable.value = newGame
    }

    fun fetchNews() {
        if (_newsLoadingState.value == LiveResource.STATE_LOADING) {
            return
        }

        _appId.value?.id?.let { id ->
            getNewsUseCase(id).apply {
                _newsLiveResource = this
                newsJob = this.job
                bindObserver(_news, this.data)
                bindObserver(_newsLoadingState, this.state)
                bindObserver(_newsLoadingError, this.error)
            }
        }
    }

    data class AppId(val id: String)
}
