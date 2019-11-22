package com.crepetete.steamachievements.ui.activity.main.fragment.library

import androidx.lifecycle.*
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.repository.UserRepository
import com.crepetete.steamachievements.repository.resource.LiveResource.Companion.STATE_LOADING
import com.crepetete.steamachievements.repository.resource.ResourceState
import com.crepetete.steamachievements.ui.common.enums.SortingType
import com.crepetete.steamachievements.vo.Game
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for handling all logic for the [LibraryFragment].
 */
class LibraryViewModel @Inject constructor(
    private val gameRepo: GameRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private companion object {
        val DEFAULT_SORT_METHOD = SortingType.PLAYTIME
    }

    // Jobs
    private val mainJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mainJob)

    private var gamesFetchJob: Job? = null

    // Games
    private val _games = MediatorLiveData<List<Game>?>()
    private val _gamesLoadingState = MediatorLiveData<@ResourceState Int?>()
    private val _gamesLoadingError = MediatorLiveData<Exception?>()

    val games: LiveData<List<Game>?> = _games
    val gamesLoadingState: LiveData<@ResourceState Int?> = _gamesLoadingState
    val gamesLoadingError: LiveData<Exception?> = _gamesLoadingError

    private var sortingType = MutableLiveData<SortingType>()

    init {
        sortingType.value = DEFAULT_SORT_METHOD
    }

    /**
     * Request an update for the [games] LiveData. If we're not fetching games already we bind our
     * observers to the corresponding values of our LiveResource.
     */
    fun fetchGames() {
        if (_gamesLoadingState.value == STATE_LOADING) {
            return
        }

        uiScope.launch {
            gameRepo.getGames(userRepository.getCurrentPlayerId()).let { resource ->
                gamesFetchJob = resource.job
                bindObserver(_games, resource.data)
                bindObserver(_gamesLoadingState, resource.state)
                bindObserver(_gamesLoadingError, resource.error)
            }
        }
    }

    fun updatePrimaryColorForGame(game: Game, rgb: Int) {
        game.setPrimaryColor(rgb)
        game.game?.let { gameData ->
            viewModelScope.launch {
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