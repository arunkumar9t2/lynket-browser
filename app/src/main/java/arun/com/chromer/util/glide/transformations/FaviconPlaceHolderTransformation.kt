/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.util.glide.transformations

import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.support.annotation.ColorInt
import arun.com.chromer.data.website.model.WebSite
import arun.com.chromer.shared.Constants
import arun.com.chromer.util.ColorUtil
import arun.com.chromer.util.Utils
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest
import java.util.*

/**
 * Created by arunk on 02-12-2017.
 */
class FaviconPlaceHolderTransformation(val website: WebSite) : BitmapTransformation() {

    private val placeholderColors = intArrayOf(
            Color.parseColor("#D32F2F"),
            Color.parseColor("#C2185B"),
            Color.parseColor("#303F9F"),
            Color.parseColor("#6A1B9A"),
            Color.parseColor("#37474F"),
            Color.parseColor("#2E7D32")
    )

    override fun equals(other: Any?): Boolean {
        return other is FaviconPlaceHolderTransformation && other.website == website
    }

    override fun hashCode(): Int = website.hashCode()

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        /* if (website.faviconUrl != null) {
             messageDigest.update(website.faviconUrl.toByteArray())
         } else {
             messageDigest.update(website.url.toByteArray())
         }*/
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        if (Utils.isValidFavicon(toTransform)) {
            return toTransform
        }
        // Draw a placeholder using theme color if it exists, else use a random color.
        val color = if (website.themeColor() != Constants.NO_COLOR) {
            website.themeColor()
        } else {
            placeholderColors[Random().nextInt(placeholderColors.size)]
        }
        return createPlaceholderImage(color, website.safeLabel(), pool, Math.max(outWidth, outHeight))
    }

    private fun createPlaceholderImage(@ColorInt color: Int, label: String, bitmapPool: BitmapPool, size: Int): Bitmap {
        val icon = bitmapPool.get(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(icon)
        val shadowRadius = Utils.dpToPx(1.8).toFloat()
        val shadowDx = Utils.dpToPx(0.1).toFloat()
        val shadowDy = Utils.dpToPx(1.0).toFloat()
        val textSize = Utils.dpToPx(24.0).toFloat()

        val bgPaint = Paint(ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            setColor(color)
            setShadowLayer(shadowRadius, shadowDx, shadowDy, Color.parseColor("#44000000"))
        }

        val padding = Utils.dpToPx(1.0)
        val corner = Utils.dpToPx(3.0)
        canvas.drawRoundRect(RectF(padding.toFloat(), padding.toFloat(), (size - padding).toFloat(), (size - padding).toFloat()),
                corner.toFloat(), corner.toFloat(), bgPaint)

        val textPaint = Paint(ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            setTextSize(textSize)
            setColor(ColorUtil.getForegroundWhiteOrBlack(color))
            style = Paint.Style.FILL
        }
        drawTextInCanvasCentre(canvas, textPaint, Utils.getFirstLetter(label).toUpperCase())
        return icon
    }

    private fun drawTextInCanvasCentre(canvas: Canvas, paint: Paint, text: String) {
        val cH = canvas.clipBounds.height()
        val cW = canvas.clipBounds.width()
        val rect = Rect()
        paint.textAlign = Paint.Align.LEFT
        paint.getTextBounds(text, 0, text.length, rect)
        val x = cW / 2f - rect.width() / 2f - rect.left.toFloat()
        val y = cH / 2f + rect.height() / 2f - rect.bottom
        canvas.drawText(text, x, y, paint)
    }
}
