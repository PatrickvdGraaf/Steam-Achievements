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
import com.crepetete.steamachievements.presentation.common.graph.AchievementsGraphViewUtil
import com.crepetete.steamachievements.util.extensions.bindObserver
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

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
    val graphData: LiveData<ArrayList<Entry>> = Transformations.map(achievements) {
        createGraphData(it)
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

    private fun createGraphData(achievements: List<Achievement>): ArrayList<Entry> {
        val unlockedAchievements = achievements
            .filter { isUnlocked(it) }
            .sortedBy { it.unlockTime }

        val achievedEntries = ArrayList<Entry>()
        val dates: MutableMap<Long, MutableList<Achievement>> = mutableMapOf()
        unlockedAchievements.forEach { unlockedAchievement ->
            unlockedAchievement.unlockTime?.let { unlockTime ->
                val sdf = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.ENGLISH
                )

                val dateString = sdf.format(unlockTime)

                sdf.parse(dateString)?.time?.let { graphDateTime ->
                    Timber.d(
                        "GRAPH: dateString: $dateString, graphDateTime: $graphDateTime}"
                    )
                    val allUnlocked =
                        achievements.filter { isUnlocked(it) && it.unlockTime?.time ?: Long.MAX_VALUE < unlockTime.time }

                    val list = dates[graphDateTime] ?: mutableListOf<Achievement>().apply {
                        addAll(allUnlocked)
                    }
                    list.add(unlockedAchievement)
                    dates[graphDateTime] = list
                }
            }
        }

        dates.forEach {
            achievedEntries.addEntry(it, achievements)
        }
        Timber.d("GRAPH: dates: ${dates.size}")

        return achievedEntries
    }

    private fun isUnlocked(achievement: Achievement?): Boolean {
        return achievement != null
                && achievement.achieved
                && achievement.unlockTime?.after(AchievementsGraphViewUtil.steamReleaseDate) == true
    }

    /**
     * This method creates an [Entry] for the [LineChart] showing Achievements completion percentages.
     *
     * It uses the size of the complete [allAchievements] list and the size of a filtered list containing
     * only unlocked Achievements to calculate the total completion percentage at the moment the user
     * unlocked the specific [achievement]. This value goes on the y-axis.
     *
     * The x-axis will contain the [Achievement.unlockTime] in millis.
     */
    private fun ArrayList<Entry>.addEntry(
        data: Map.Entry<Long, MutableList<Achievement>>,
        allAchievements: List<Achievement>
    ) {
        val unlockDayMillis = data.key

        // Check the users completion rate after unlocking the [achievement].
        val unlockedAchievements = data.value

        // Calculate the percentage relative to the already achieved achievements at that time.
        val completionPercentage =
            (unlockedAchievements.size.toFloat() / allAchievements.size.toFloat())

        this.add(Entry(unlockDayMillis.toFloat(), completionPercentage * 100F))
    }

    data class AppId(val id: String)
}
