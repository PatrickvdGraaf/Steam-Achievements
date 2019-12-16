package com.crepetete.steamachievements.util

object StringUtils {
    /**
     * Abbreviates the given [text] to a length of [maxLength] plus a text indicating that there is
     * more text available.
     */
    fun limitTextLength(text: String, maxLength: Int = 600): String {
        if (text.length < maxLength) {
            return text
        }

        // TODO: make into a String resource.
        // TODO: create a click listener for the more... text.
        return "${text.substring(0, maxLength)}.. (more...)"
    }

    /**
     * Search for an Image Url in the text based on the assumption that an (optional) image url is
     * placed at the beginning of an news article in the json that Steam provides, so we start at
     * the index of the first 'http' that we find and end the url when an image file type is
     * detected.
     * At the moment we search for JPEGs and PNGs.
     *
     * A negative index means that the index could not be found.
     *
     * Note: this is not the ideal situation but it makes the most of the limited data that the
     * Steam API provides. In a perfect world we wouldn't have to extract an optional image url from
     * an (sometimes attributed) String, but instead retrieve it from the backend as a separate
     * value.
     */
    fun findImageUrlInText(text: String): String? {
        val urlStartIndex = text.indexOf("http")
        var urlEndIndex = text.indexOf(".jpg")
        if (urlEndIndex == -1) {
            urlEndIndex = text.indexOf(".png")
        }

        val areIndexesCorrect =
            urlStartIndex != -1 && urlEndIndex != -1 && urlStartIndex < urlEndIndex

        return if (areIndexesCorrect) {
            // Note that 4 stands for '.jpg' /'.png' length.
            text.substring(urlStartIndex, urlEndIndex + 4)
        } else {
            null
        }
    }
}