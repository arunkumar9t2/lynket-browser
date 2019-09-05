package arun.com.chromer.bubbles

import android.content.Context

interface FloatingBubble {

    fun openBubble(
            url: String,
            fromMinimize: Boolean,
            fromAmp: Boolean,
            incognito: Boolean,
            context: Context? = null
    )
}