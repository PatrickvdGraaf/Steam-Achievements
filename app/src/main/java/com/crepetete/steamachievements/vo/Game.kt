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
class Game(
    @Embedded
    var game: BaseGameInfo? = null,

    @Relation(parentColumn = "appId", entityColumn = "appId", entity = Achievement::class)
    var achievements: List<Achievement> = listOf()
) : Parcelable {

    companion object {
        const val INVALID_COLOR = 0
    }

    fun setPrimaryColor(color: Int) {
        game?.colorPrimaryDark = color
    }

    fun getPercentageCompleted(): Float {
        val achievedSize = achievements.filter { it.achieved }.size.toFloat()
        val totalSize = achievements.size.toFloat()
        return achievedSize / totalSize * 100F
    }

    fun getAmountOfAchievements() = achievements.size
    fun getRecentPlaytime() = game?.recentPlayTime ?: 0
    fun getPrimaryColor() = game?.colorPrimaryDark ?: INVALID_COLOR
    fun getAppId(): Long = game?.appId ?: -1L
    fun getName() = game?.name ?: ""
    fun getPlaytime() = game?.playTime ?: 0L
    fun getBannerUrl() = "http://media.steampowered.com/steamcommunity/public/images/apps/${game?.appId ?: "0"}/${game?.logoUrl
        ?: ""}.jpg"

}