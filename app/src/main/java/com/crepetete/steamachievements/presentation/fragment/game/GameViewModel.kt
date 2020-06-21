package com.crepetete.steamachievements.presentation.fragment.game

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.data.api.response.news.NewsItem
import com.crepetete.steamachievements.data.helper.LiveResource
import com.crepetete.steamachievements.data.helper.ResourceState
import com.crepetete.steamachievements.domain.model.Achievement
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.domain.usecases.achievements.GetAchievementsUseCase
import com.crepetete.steamachievements.domain.usecases.game.GetGameUseCase
import com.crepetete.steamachievements.domain.usecases.news.GetNewsSnapshotUseCase
import com.crepetete.steamachievements.domain.usecases.news.UpdateNewsUseCase
import com.crepetete.steamachievements.presentation.common.adapter.sorting.AchievementSortedListImpl
import com.crepetete.steamachievements.presentation.common.adapter.sorting.Order
import com.crepetete.steamachievements.util.extensions.bindObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class GameViewModel(
    private val getGameUseCase: GetGameUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val getNewsUseCase: GetNewsSnapshotUseCase,
    private val updateNewsUseCase: UpdateNewsUseCase
) : ViewModel() {

    private val mainJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mainJob)
    private var newsJob: Job? = null

    // ID
    private val _appId = MutableLiveData<AppId>()
    val appId: LiveData<AppId>
        get() = _appId

    // Api
    private var _newsLiveResource: LiveResource? = null
    private val _newsLoadingState = MediatorLiveData<@ResourceState Int?>()
    val newsLoadingState: LiveData<@ResourceState Int?> = _newsLoadingState

    private val _newsLoadingError = MediatorLiveData<Exception?>()
    val newsLoadingError: LiveData<Exception?> = _newsLoadingError

    // Room
    val game: LiveData<BaseGameInfo> = Transformations.switchMap(appId) {
        getGameUseCase(it.id)
    }

    val achievements: LiveData<List<Achievement>> = Transformations.switchMap(appId) {
        getAchievementsUseCase(it.id)
    }

    val news: LiveData<List<NewsItem>> = Transformations.switchMap(appId) {
        getNewsUseCase(it.id)
    }

    // Chart
    private val _achievementsChartData = MutableLiveData<List<Achievement?>>()
    val achievementsChartData: LiveData<List<Achievement?>>
        get() = _achievementsChartData

    // Sort
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
        val id = newGame.getAppId().toString()
        _appId.value = AppId(id)
        updateNews(id)
    }

    private fun updateNews(appId: String) {
        if (_newsLoadingState.value == LiveResource.STATE_LOADING) {
            return
        }

        updateNewsUseCase(appId).apply {
            _newsLiveResource = this
            newsJob = this.job
            bindObserver(_newsLoadingState, this.state)
            bindObserver(_newsLoadingError, this.error)
        }
    }

    data class AppId(val id: String)
}
