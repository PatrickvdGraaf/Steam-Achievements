package com.crepetete.steamachievements.domain.usecases.achievements

import com.crepetete.steamachievements.domain.model.Player
import com.crepetete.steamachievements.domain.repository.AchievementsRepository
import com.crepetete.steamachievements.domain.repository.PlayerRepository

/**
 * @author: Patrick van de Graaf.
 * @date: Wed 10 Jun, 2020; 14:50.
 */
class UpdateAchievementsUseCaseImpl(
    private val achievementsRepository: AchievementsRepository,
    private val playerRepository: PlayerRepository
) : UpdateAchievementsUseCase {
    override suspend fun invoke(userId: String?, appId: String) {
        val id = checkId(userId)
        if (id != Player.INVALID_ID) {
            achievementsRepository.updateAchievementsFromApi(id, appId)
        }
    }

    override suspend fun invoke(userId: String?, appIds: List<String>) {
        val id = checkId(userId)
        if (id != Player.INVALID_ID) {
            appIds.forEach {
                achievementsRepository.updateAchievementsFromApi(id, it)
            }
        }
    }

    /**
     * Checks if the passed [userId] is valid.
     * If it's not null, we've want to retrieve the games for a specific user.
     * If it is null, then we fall back to the ID of the current user.
     * [PlayerRepository.getCurrentPlayerId] returns [Player.INVALID_ID] when no current user id is
     * found.
     */
    private fun checkId(userId: String?): String {
        return userId ?: playerRepository.getCurrentPlayerId()
    }
}