package arun.com.chromer.webheads

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