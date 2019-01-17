package com.crepetete.steamachievements.vo

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.android.parcel.Parcelize

/**
 * Combined database model with a game and its achievements.
 *
 * Uses experimental feature [Parcelize], which removes boilerplate code.
 * More info;
 * @see <a href="https://proandroiddev.com/parcelable-in-kotlin-here-comes-parcelize-b998d5a5fcac">this link</a>.
 */
@Parcelize
class GameWithAchievements(
    @Embedded
    var game: Game? = null,

    @Relation(parentColumn = "appId", entityColumn = "appId", entity = Achievement::class)
    var achievements: List<Achievement> = listOf()
): Parcelable {

    fun getPercentageCompleted(): Float {
        val achievedSize = achievements.filter { it.achieved }.size.toFloat()
        val totalSize = achievements.size.toFloat()
        return achievedSize / totalSize * 100F
    }

    fun getAmountOfAchievements() = achievements.size
    fun getAchievedAchievements() = achievements.filter { achievement -> achievement.achieved }
    fun getRecentPlaytime() = game?.recentPlayTime ?: 0
    fun getPrimaryColor() = game?.colorPrimaryDark ?: 0
    fun getAppId() = game?.appId ?: ""
    fun getName() = game?.name ?: ""
    fun getPlaytime() = game?.playTime ?: 0L
    fun getIconUrl() = game?.iconUrl ?: ""
    fun getBannerUrl() = game?.logoUrl ?: ""

}