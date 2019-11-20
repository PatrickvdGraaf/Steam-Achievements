package com.crepetete.steamachievements.ui.activity.main.fragment.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crepetete.steamachievements.repository.GameRepository
import com.crepetete.steamachievements.repository.UserRepository
import com.crepetete.steamachievements.repository.resource.LiveResource
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
 * ViewModel responsible for the [LibraryFragment].
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
    private val ioScope = CoroutineScope(Dispatchers.IO + mainJob)

    private var gamesFetchJob: Job? = null

    // Games
    private var gamesLiveResource: LiveResource<List<Game>>? = null

    private val _games = MediatorLiveData<List<Game>?>()
    private val _gamesLoadingState = MediatorLiveData<@ResourceState Int?>()
    private val _gamesLoadingError = MediatorLiveData<Exception?>()

    private var sortingType = MutableLiveData<SortingType>()

    val games: LiveData<List<Game>?> = _games
    val gamesLoadingState: LiveData<@ResourceState Int?> = _gamesLoadingState
    val gamesLoadingError: LiveData<Exception?> = _gamesLoadingError

    init {
        sortingType.value = DEFAULT_SORT_METHOD
    }

    fun fetchGames() {
        if (_gamesLoadingState.value == STATE_LOADING) {
            return
        }

        gameRepo.getGames(userRepository.getCurrentPlayerId())
            .let { resource ->
                gamesLiveResource = resource
                gamesFetchJob = resource.job
                bindObserver(_games, resource.data)
                bindObserver(_gamesLoadingState, resource.state)
                bindObserver(_gamesLoadingError, resource.error)
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