package com.crepetete.steamachievements.util

object StringUtils {
    /**
     * Abbreviates the given [text] to a length of [maxLength] and adds three dots after the last
     * word before the [maxLength]indicating that there
     * is more text available.
     *
     */
    fun limitTextLength(text: String, maxLength: Int = 600): String {
        if (text.length < maxLength) {
            return text
        }

        val cappedText = text.substring(0, maxLength)

        // Find the last word in our capped text, and get it's length.
        val parts: List<String> = cappedText.split(" ")
        // Plus one for a space or dot in front of the word.
        val lastWordLength = parts[parts.size - 1].length + 1

        // Use the length to remove the last word in the text to remove words that were split when
        // we capped the original text.
        val trimmedText = cappedText.substring(0, cappedText.length - lastWordLength)

        return "$trimmedText..."
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
        val regex = "http[^\\s]+(jpg|jpeg|png|tiff)\\b".toRegex()
        return regex.find(text)?.value
    }
}