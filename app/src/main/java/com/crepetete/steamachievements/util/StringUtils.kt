package com.crepetete.steamachievements.util

import android.content.Context

object StringUtils {
    fun limitTextLength(context: Context?, text: String, maxLength: Int = 600): String {
        // TODO: make into String resource.
        if (text.length < maxLength) {
            return text
        }
        return "${text.substring(0, maxLength)}.. (more...)"
    }

    fun findImageUrlInText(text: String): String? {
        val urlStartIndex = text.indexOf("http")
        var urlEndIndex = text.indexOf(".jpg")
        if (urlEndIndex == -1) {
            urlEndIndex = text.indexOf(".png")
        }

        return if (areIndexesCorrect(urlStartIndex, urlEndIndex)) {
            text.substring(urlStartIndex, urlEndIndex + 4) // Note that 4 stands for '.jpg' /'.png'.
        } else {
            null
        }
    }

    private fun areIndexesCorrect(start: Int, end: Int): Boolean {
        return start != -1 && end != -1 && start < end
    }
}