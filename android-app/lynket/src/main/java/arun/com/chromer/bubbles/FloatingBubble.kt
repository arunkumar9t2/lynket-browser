/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.bubbles

import android.content.Context
import androidx.annotation.ColorInt
import arun.com.chromer.bubbles.BubbleType.NATIVE
import arun.com.chromer.bubbles.BubbleType.WEB_HEADS
import arun.com.chromer.bubbles.system.NativeFloatingBubble
import arun.com.chromer.bubbles.webheads.WebHeadsFloatingBubble
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.shared.Constants
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

interface FloatingBubble {

  fun openBubble(
    website: Website,
    fromMinimize: Boolean,
    fromAmp: Boolean,
    incognito: Boolean,
    context: Context? = null,
    @ColorInt
    color: Int = Constants.NO_COLOR
  )
}


enum class BubbleType {
  NATIVE,
  WEB_HEADS
}

@Singleton
class FloatingBubbleFactory
@Inject
constructor(
  private val nativeFloatingBubble: Provider<NativeFloatingBubble>,
  private val webHeadsFloatingBubble: Provider<WebHeadsFloatingBubble>
) {
  operator fun get(bubble: BubbleType): FloatingBubble = when (bubble) {
    NATIVE -> nativeFloatingBubble.get()
    WEB_HEADS -> webHeadsFloatingBubble.get()
  }
}
