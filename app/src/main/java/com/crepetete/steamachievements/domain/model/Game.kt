package com.crepetete.steamachievements.domain.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import com.crepetete.steamachievements.BuildConfig
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
        private const val INVALID_COLOR = 0
        private const val INVALID_ID = -1

        // Killing Floor 2
        const val TEST_GAME_ID = 232090
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
    fun getAmountOfUnlockedAchievements() = achievements.filter { it.achieved }.size
    fun getRecentPlaytime() = game?.recentPlayTime ?: 0
    fun getPrimaryColor() = game?.colorPrimaryDark ?: INVALID_COLOR
    fun getAppId(): Int = game?.appId ?: if (BuildConfig.DEBUG) TEST_GAME_ID else INVALID_ID
    fun getName() = game?.name ?: ""
    fun getPlaytime() = game?.playTime ?: 0
    fun getBannerUrl() =
        "http://media.steampowered.com/steamcommunity/public/images/apps" +
                "/${game?.appId}" +
                "/${game?.logoUrl}" +
                ".jpg"

    override fun equals(other: Any?): Boolean {
        return if (other is Game) {
            (other.getName() == getName() &&
                    other.getBannerUrl() == getBannerUrl() &&
                    other.getAmountOfAchievements() == getAmountOfAchievements() &&
                    other.getPercentageCompleted() == getPercentageCompleted() &&
                    other.getPlaytime() == getPlaytime() &&
                    other.getRecentPlaytime() == getRecentPlaytime())
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return getAppId().toInt()
    }
}