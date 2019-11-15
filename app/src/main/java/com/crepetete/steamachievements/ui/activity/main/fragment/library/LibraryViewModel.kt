package com.crepetete.steamachievements.ui.activity.main.fragment.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.AchievementsRepository
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.repository.UserRepository
import com.crepetete.steamachievements.repository.resource.LiveResource
import com.crepetete.steamachievements.repository.resource.LiveResource.Companion.STATE_LOADING
import com.crepetete.steamachievements.repository.resource.ResourceState
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.util.extensions.sort
import com.crepetete.steamachievements.util.livedata.CombinedLiveData
import com.crepetete.steamachievements.vo.Achievement
import com.crepetete.steamachievements.vo.BaseGameInfo
import com.crepetete.steamachievements.vo.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for the [LibraryFragment].
 */
class LibraryViewModel @Inject constructor(
    private val gameRepo: GameRepository,
    private val achievementsRepository: AchievementsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private companion object {
        val DEFAULT_SORT_METHOD = SortingType.PLAYTIME
    }

    // Jobs
    private val mainJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mainJob)
    private val ioScope = CoroutineScope(Dispatchers.IO + mainJob)
    private var gamesFetchJob: Job? = null

    // Games
    private var gamesLiveResource: LiveResource<List<BaseGameInfo>>? = null
    private val _games = MediatorLiveData<List<BaseGameInfo>?>()
    private val _gamesLoadingState = MediatorLiveData<@ResourceState Int?>()
    private val _gamesLoadingError = MediatorLiveData<Exception?>()

    private var sortingType = MutableLiveData<SortingType>()

    val games: LiveData<List<BaseGameInfo>?> = _games
    val gamesLoadingState: LiveData<@ResourceState Int?> = _gamesLoadingState
    val gamesLoadingError: LiveData<Exception?> = _gamesLoadingError

    // Achievements
    private val _achievements = achievementsRepository.fetchAllAchievements()

    // Combined data
    val data: CombinedLiveData<List<BaseGameInfo>,
        List<Achievement>,
        List<Game>> = CombinedLiveData(_games, _achievements) { gamesInfo, achievements ->
        val games = mutableListOf<Game>()
        gamesInfo?.forEach { gameInfo ->
            games.add(Game(gameInfo, achievements?.filter { achievement ->
                achievement.appId == gameInfo.appId
            } ?: listOf()))
        }
        games.sort(sortingType.value ?: DEFAULT_SORT_METHOD)

    }

    init {
        sortingType.value = DEFAULT_SORT_METHOD
    }

    fun fetchGames() {
        if (_gamesLoadingState.value == STATE_LOADING) {
            return
        }

        gameRepo.getGames(userRepository.getCurrentPlayerId())
            .let { resource ->
                uiScope.launch {
                    gamesLiveResource = resource
                    gamesFetchJob = resource.job
                    bindObserver(_games, resource.data)
                    bindObserver(_gamesLoadingState, resource.state)
                    bindObserver(_gamesLoadingError, resource.error)
                }
            }

    }

    fun updateAchievementsForGame(appId: String) {
        ioScope.launch {
            achievementsRepository.fetchAchievementsFromApi(appId)
        }
    }

    fun updatePrimaryColorForGame(game: Game, rgb: Int) {
        game.setPrimaryColor(rgb)
        game.game?.let { gameData ->
            ioScope.launch {
                gameRepo.update(gameData)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mainJob.cancel()
    }

    private fun <R> bindObserver(observer: MediatorLiveData<R>, source: LiveData<R>) {
        observer.apply {
            addSource(source) {
                postValue(it)
            }
        }
    }
}