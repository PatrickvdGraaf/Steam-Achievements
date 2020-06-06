package com.crepetete.steamachievements.util

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import com.crepetete.steamachievements.R

object StringUtils {
    /**
     * Abbreviates the given [text] to a length of [maxLength] plus a text indicating that there is
     * more text available.
     * An optional [View.OnClickListener] can be passed if something needs to be done when the
     * 'more' text is clicked.
     */
    fun limitTextLength(
        context: Context,
        text: String,
        maxLength: Int = 600,
        onMoreClickedListener: View.OnClickListener? = null
    ): SpannableString {
        if (text.length < maxLength) {
            return SpannableString(text)
        }

        // "%s.. [%s]"
        val limiterFormatText = context.getString(R.string.format_limiter_more)

        // e.g. "more..."
        val textMore = context.getString(R.string.limiter_more)

        // Set the shortened text and set the translated text for 'More'.
        val spannableString = SpannableString(
            String.format(
                limiterFormatText,
                text.substring(0, maxLength),
                textMore
            )
        )

        // Use the textMore to calculate where the clickable span should be.
        // It should be the total length of the text, minus one for the ')', minus the length of the
        // 'more' text in the users current localisation.
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onMoreClickedListener?.onClick(widget)
                }
            },
            spannableString.length - (1 + textMore.length),
            spannableString.length - 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
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