package arun.com.chromer.bubbles

import arun.com.chromer.bubbles.system.NativeFloatingBubble
import arun.com.chromer.bubbles.webheads.WebHeadsFloatingBubble
import dagger.Module
import dagger.Provides

@Module
class BubbleModule {

    @Provides
    fun floatingBubble(
            webHeadsFloatingBubble: WebHeadsFloatingBubble,
            nativeFloatingBubble: NativeFloatingBubble
    ): FloatingBubble {
        return nativeFloatingBubble
    }
}