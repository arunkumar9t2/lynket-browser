@file:Suppress("NOTHING_TO_INLINE")

package arun.com.chromer.util

import android.graphics.Typeface
import android.text.Spannable
import android.text.style.StyleSpan
import androidx.core.text.set

inline fun Spannable.makeMatchingBold(query: String): Spannable {
    if (query.isNotEmpty()) {
        val start = indexOf(query, ignoreCase = true)
        if (start != -1) {
            this[start, start + query.length] = StyleSpan(Typeface.BOLD)
        }
    }
    return this
}