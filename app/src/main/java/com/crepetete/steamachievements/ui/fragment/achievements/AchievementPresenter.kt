package com.crepetete.steamachievements.ui.fragment.achievements

import com.crepetete.steamachievements.base.BasePresenter
import com.crepetete.steamachievements.data.repository.achievement.AchievementRepository
import com.crepetete.steamachievements.model.Achievement
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class AchievementPresenter(achievementsView: AchievementsView)
    : BasePresenter<AchievementsView>(achievementsView) {
    private var disposable: CompositeDisposable = CompositeDisposable()

    @Inject
    lateinit var achievementsRepository: AchievementRepository

    override fun onViewCreated() {
        loadAchievementStats()
    }

    private fun loadAchievementStats() {
        disposable.add(achievementsRepository.getAllAchievements()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    //                    val allAchievements = it.size
//                    val allPlayerAchievements = it.filter { a -> a.achieved }.size
//                    val completionPercentage = if (allAchievements > 0) {
//                        (allPlayerAchievements.toDouble() / allAchievements.toDouble()) * 100.0
//                    } else {
//                        0.0
//                    }
//
//                    view.setTotalAchievementsInfo(allPlayerAchievements)
//                    view.setCompletionPercentage(completionPercentage)

                    view.setTotalAchievementsInfo(it.filter { it.achieved }.size)
                    val achievementsForGame = HashMap<String, MutableList<Achievement>>()
                    it.forEach { achievement ->
                        if (achievementsForGame.containsKey(achievement.appId)) {
                            achievementsForGame[achievement.appId]?.add(achievement)
                        } else {
                            achievementsForGame[achievement.appId] = mutableListOf()
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
                                percentages.add((achievedAchievements.size.toDouble() / allAchievements.size.toDouble()) * 100.0)
                            }
                        }
                    }
                    var percentagesSum = 0.0
                    percentages.forEach { percentagesSum += it }
                    val percentage = (percentagesSum / (percentages.size * 100.0) * 100.0)
                    view.setCompletionPercentage(percentage)

                }, {
                    Timber.e(it)
                }))
    }
}