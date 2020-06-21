package com.crepetete.steamachievements.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.crepetete.steamachievements.domain.model.BaseGameInfo
import com.crepetete.steamachievements.testing.OpenForTesting
import kotlinx.coroutines.flow.Flow

/**
 * Interface for database access on [BaseGameInfo] related operations.
 */
@Dao
@OpenForTesting
abstract class GamesDao : BaseDao<BaseGameInfo>() {
    @Transaction
    @Query("SELECT * FROM games WHERE appId = :appId LIMIT 1")
    abstract fun getGame(appId: String): Flow<BaseGameInfo>

    // https://medium.com/androiddevelopers/room-flow-273acffe5b57
    @Transaction
    @Query("SELECT * FROM games ORDER BY playTime DESC")
    abstract fun getGames(): Flow<List<BaseGameInfo>?>

    @Transaction
    @Query("SELECT * FROM games WHERE userId = :userId ORDER BY playTime DESC")
    abstract fun getGames(userId: String): List<BaseGameInfo>

    @Transaction
    @Query("SELECT appId FROM games")
    abstract fun getGameIds(userId: String): List<String>
}