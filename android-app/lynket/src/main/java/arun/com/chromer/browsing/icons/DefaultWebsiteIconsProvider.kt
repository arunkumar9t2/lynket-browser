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

package arun.com.chromer.browsing.icons

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Color.WHITE
import android.graphics.Color.parseColor
import android.graphics.Paint
import androidx.palette.graphics.Palette
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.util.ColorUtil
import arun.com.chromer.util.glide.GlideApp
import dev.arunkumar.common.context.dpToPx
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max


@Singleton
class DefaultWebsiteIconsProvider
@Inject
constructor(private val application: Application) : WebsiteIconsProvider {

  private val placeholderColors = intArrayOf(
    parseColor("#D32F2F"),
    parseColor("#C2185B"),
    parseColor("#303F9F"),
    parseColor("#6A1B9A"),
    parseColor("#37474F"),
    parseColor("#2E7D32")
  )

  private val randomColor get() = placeholderColors.random()

  // https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive
  private val adaptiveIconOuterSize by lazy { application.dpToPx(108.0) }

  override fun getBubbleIconAndColor(website: Website): Single<WebsiteIconData> {
    return Single.fromCallable {
      val websiteIcon = GlideApp.with(application)
        .asBitmap()
        .load(website)
        .submit(adaptiveIconOuterSize, adaptiveIconOuterSize)
        .get()
      val palette = Palette.from(websiteIcon).clearFilters().generate()
      return@fromCallable WebsiteIconData(
        website = website,
        icon = padBitmap(websiteIcon),
        color = ColorUtil.getBestColorFromPalette(palette)
      )
    }
  }

  /**
   * Returns a new bitmap that is padded 25% on all sizes. Required to prevent blurry icons.
   *
   * https://source.android.com/devices/tech/display/adaptive-icons
   */
  private fun padBitmap(sourceBitmap: Bitmap): Bitmap {
    val padding = (max(sourceBitmap.height, sourceBitmap.width) * 0.25).toInt()
    val paddedBitmap = createBitmap(
      sourceBitmap.width + padding,
      sourceBitmap.height + padding,
      ARGB_8888
    )
    val canvas = Canvas(paddedBitmap)
    canvas.drawColor(WHITE) // TODO Handle for dark mode
    canvas.drawBitmap(
      sourceBitmap,
      (padding / 2).toFloat(),
      (padding / 2).toFloat(),
      Paint(Paint.FILTER_BITMAP_FLAG)
    )
    return paddedBitmap
  }
}
