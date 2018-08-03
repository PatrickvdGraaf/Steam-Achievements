package com.crepetete.steamachievements.ui.fragment.achievements

import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
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

    override fun onViewCreated() {
        loadAchievementStats()
        loadBestDay()
    }

    private fun loadBestDay(){
        disposable.add(achievementsRepository.getBestAchievementsDay()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.showBestDay(it)
                }, {
                    Timber.e(it)
                }))
    }

    private fun loadAchievementStats() {
        disposable.add(achievementsRepository.getAllAchievements()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    view.setTotalAchievementsInfo(it.filter { it.achieved }.size)
                    calculateCompletionPercentage(it)
                    view.showLatestAchievements(getLatestAchievements(it, it.size))
                }, {
                    Timber.e(it)
                }))
    }

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

    private fun getLatestAchievements(achievements: List<Achievement>, size: Int = 20): List<Achievement> {
        return achievements.filter { it.achieved }.sortByLastAchieved().take(size)
    }
}