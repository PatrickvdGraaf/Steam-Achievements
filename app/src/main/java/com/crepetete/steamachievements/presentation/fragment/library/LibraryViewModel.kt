package com.crepetete.steamachievements.presentation.fragment.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.crepetete.data.helper.LiveResource.Companion.STATE_LOADING
import com.crepetete.data.helper.ResourceState
import com.crepetete.steamachievements.domain.model.Game
import com.crepetete.steamachievements.domain.usecases.game.GetGamesUseCase
import com.crepetete.steamachievements.presentation.common.enums.SortingType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for handling all logic for the [LibraryFragment].
 */
class LibraryViewModel(private val getGamesUseCase: GetGamesUseCase) : ViewModel() {

    private companion object {
        val DEFAULT_SORT_METHOD = SortingType.PLAYTIME
    }

    // Jobs
    private val mainJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + mainJob)

    private var gamesFetchJob: Job? = null

    // Games
    private val _games = MediatorLiveData<List<Game>?>()
    val games: LiveData<List<Game>?> = _games

    // Error
    private val _gamesLoadingError = MediatorLiveData<Exception?>()
    val gamesLoadingError: LiveData<Exception?> = _gamesLoadingError

    // Loading State
    private val _gamesLoadingState = MediatorLiveData<@ResourceState Int?>()
    val gamesLoadingState: LiveData<@ResourceState Int?> = _gamesLoadingState

    // Sorting
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
            getGamesUseCase().let { resource ->
                gamesFetchJob = resource.job
                bindObserver(_games, resource.data)
                bindObserver(_gamesLoadingState, resource.state)
                bindObserver(_gamesLoadingError, resource.error)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mainJob.cancel()
        gamesFetchJob?.cancel()
    }

    private fun <R> bindObserver(observer: MediatorLiveData<R>, source: LiveData<R>) {
        observer.apply {
            addSource(source) { src ->
                postValue(src)
            }
        }
    }
}