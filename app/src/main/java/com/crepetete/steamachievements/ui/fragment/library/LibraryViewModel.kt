package com.crepetete.steamachievements.ui.fragment.library

import android.arch.lifecycle.*
import com.crepetete.steamachievements.data.database.model.GameWithAchievements
import com.crepetete.steamachievements.data.repository.achievement.AchievementsRepository
import com.crepetete.steamachievements.data.repository.game.GameRepository
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.model.Game
import com.crepetete.steamachievements.ui.common.adapter.games.SortingType
import com.crepetete.steamachievements.utils.AbsentLiveData
import com.crepetete.steamachievements.utils.resource.Resource
import com.crepetete.steamachievements.utils.sort
import javax.inject.Inject


class LibraryViewModel @Inject constructor(private var gameRepo: GameRepository,
                                           private var achievementsRepository: AchievementsRepository) : ViewModel() {
    private val _userId = MutableLiveData<UserId>()
    val userId: LiveData<UserId>
        get() = _userId

    val games: LiveData<Resource<List<GameWithAchievements>>> = Transformations
            .switchMap(_userId) { id ->
                id.ifExists {
                    gameRepo.getGames(it)
                }
            }

    val achievements = achievementsRepository.getAchievementsFromDb()

    val finalData = MediatorLiveData<List<Game>>()

    private var sortingType = SortingType.PLAYTIME

    init {
        finalData.addSource(games) { resource ->
            resource?.data?.asSequence()?.mapNotNull { it.game }?.toList()
                    ?.let {
                        val games = mutableListOf<Game>()
                        it.forEach { game ->
                            achievements.value?.filter { a -> a.appId == game.appId }.let { filteredAchievements ->
                                if (filteredAchievements != null) {
                                    game.setAchievements(filteredAchievements)
                                }
                            }
                            games.add(game)
                        }
                        finalData.value = games.sort(sortingType)
                    }
        }

        finalData.addSource(achievements) { achievements ->
            if (achievements != null) {
                val games = finalData.value
                games?.forEach { game ->
                    game.setAchievements(achievements.filter { achievement ->
                        achievement.appId == game.appId
                    })
                }
                finalData.value = games
            }
        }
    }

    fun rearrangeGames(order: SortingType) = games.value?.data?.map { it.game }?.let {
        val nonNullGames = mutableListOf<Game>()
        it.forEach { game ->
            if (game != null) {
                nonNullGames.add(game)
            }
        }
        finalData.value = nonNullGames.toList().sort(order)
    }.also { sortingType = order }

    fun updateAchievementsFor(appId: String) {
        achievementsRepository.loadAchievementsForGame(appId)
    }

    fun updateAchievedStats(appId: String, achievements: List<Achievement>) {
//        if (achievements.isNotEmpty()) {
//            achievementsRepository.getAchievedStatusForAchievementsForGame(appId, achievements)
//        }
    }

    fun updateGlobalStats(appId: String, achievements: List<Achievement>) {
//        if (achievements.isNotEmpty()) {
//            achievementsRepository.getGlobalAchievementStats(appId, achievements)
//        }
    }

    fun setAppId(appId: String) {
        val update = UserId(appId)
        if (_userId.value == update) {
            return
        }
        _userId.value = update
    }

    data class UserId(val id: String) {
        fun <T> ifExists(f: (String) -> LiveData<T>): LiveData<T> {
            return if (id.isBlank()) {
                AbsentLiveData.create()
            } else {
                f(id)
            }
        }
    }
}