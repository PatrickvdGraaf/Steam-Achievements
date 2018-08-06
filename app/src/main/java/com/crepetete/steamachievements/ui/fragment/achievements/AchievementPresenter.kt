package com.crepetete.steamachievements.ui.fragment.achievements

import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.data.repository.game.GamesRepository
import com.crepetete.steamachievements.model.Achievement
import com.crepetete.steamachievements.utils.sortByLastAchieved
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class AchievementPresenter(achievementsView: AchievementsView)
    : BasePresenter<AchievementsView>(achievementsView) {
    @Inject
    lateinit var achievementsRepository: AchievementRepository

    @Inject
    lateinit var gamesRepository: GamesRepository

    /**
     * Loads all achievements from the database once the view is created. It also starts the
     * calculation to determine the users best achievements day.
     */
    override fun onViewCreated() {
        loadAchievementStats()
        loadBestDay()
    }

    /**
     * Loads all achievements from the database. It then uses this list to update the Total
     * Achievements TextView with its size, to calculate the global completion rate, and to display
     * them in the View. Finally, it updates the achievements.
     */
    private fun loadAchievementStats() {
        disposable.add(achievementsRepository.getAllAchievementsFromDb()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.setTotalAchievementsInfo(it.filter { achievement ->
                        achievement.achieved
                    }.size)
                    calculateCompletionPercentage(it)
                    view.showLatestAchievements(getLatestAchievements(it, it.size))
                }, {
                    Timber.e(it)
                }))
    }

    /**
     * Finds the day on which the user achieved the most Achievements and updates the view with this
     * new info.
     */
    private fun loadBestDay() {
        disposable.add(achievementsRepository.getBestAchievementsDay()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.showBestDay(it)
                }, {
                    Timber.e(it)
                }))
    }

    private fun updateAchievements() {
        disposable.add(gamesRepository.getGameIds()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    getAchievementsFromApi(it)
                }, {
                    Timber.e(it)
                }))
    }

    private fun getAchievementsFromApi(games: List<String>) {
        disposable.add(achievementsRepository.getAchievementsFromApi(games)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({

                }, {
                    Timber.e(it)
                }))
    }

    /**
     * Calculates the global completion percentage for a user by first sorting all achievements on
     * their appId. This means sorting the achievements with the game they belong to. Then, it
     * iterates over these games and their list of achievements, calculating their individual
     * completion rates and saving them in an ArrayList. Finally, we calculate the global percentage
     * and update the view with it.
     */
    private fun calculateCompletionPercentage(achievements: List<Achievement>) {
        val achievementsForGame = HashMap<String, MutableList<Achievement>>()
        achievements.map {
            if (!achievementsForGame.containsKey(it.appId)) {
                achievementsForGame[it.appId] = mutableListOf(it)
            } else {
                achievementsForGame[it.appId]!!.add(it)
            }
        }

        val percentages = mutableListOf<Double>()
        achievementsForGame.keys.forEach { key ->
            val allAchievements = achievementsForGame[key]?.toList()
            if (allAchievements != null) {
                val achievedAchievements = allAchievements.filter {
                    it.achieved
                }
                if (achievedAchievements.isNotEmpty()) {
                    percentages.add((achievedAchievements.size.toDouble()
                            / allAchievements.size.toDouble()) * 100.0)
                }
            }
        }


        var percentagesSum = 0.0
        percentages.forEach { percentagesSum += it }
        val percentage = (percentagesSum / (percentages.size * 100.0) * 100.0)
        view.setCompletionPercentage(percentage)
    }

    /**
     * Returns a list of recently achieved Achievements.
     *
     * @param size optional parameter that can be used to determine the size of the returned list.
     * @return a list of all achieved achievements, sorted by achievement-date and containing 20
     * items unless specified otherwise via the [size] parameter.
     */
    private fun getLatestAchievements(achievements: List<Achievement>, size: Int = 20): List<Achievement> {
        return achievements.filter { it.achieved }.sortByLastAchieved().take(size)
    }
}