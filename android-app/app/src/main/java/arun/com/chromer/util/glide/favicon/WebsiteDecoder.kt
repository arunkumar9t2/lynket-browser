/*
 * Lynket
 *
 * Copyright (C) 2018 Arunkumar
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

package arun.com.chromer.util.glide.favicon

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.support.annotation.ColorInt
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.shared.Constants
import arun.com.chromer.util.ColorUtil
import arun.com.chromer.util.Utils
import arun.com.chromer.util.glide.GlideApp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import timber.log.Timber
import java.util.*

class WebsiteDecoder(private val context: Context, glide: Glide) : ResourceDecoder<Website, Bitmap> {
    private val bitmapPool: BitmapPool = glide.bitmapPool
    val size = Utils.dpToPx(56.0)

    private val placeholderColors = intArrayOf(
            Color.parseColor("#D32F2F"),
            Color.parseColor("#C2185B"),
            Color.parseColor("#303F9F"),
            Color.parseColor("#6A1B9A"),
            Color.parseColor("#37474F"),
            Color.parseColor("#2E7D32")
    )

    override fun handles(source: Website, options: Options?): Boolean = true

    override fun decode(website: Website, width: Int, height: Int, options: Options?): Resource<Bitmap>? {
        // Try to load using Glide normally
        val websiteFavicon = try {
            GlideApp.with(context)
                    .asBitmap()
                    .load(website.faviconUrl)
                    .useUnlimitedSourceGeneratorsPool(true)
                    .submit()
                    .get()
        } catch (e: Exception) {
            null
        }
        return try {
            if (Utils.isValidFavicon(websiteFavicon)) {
                BitmapResource.obtain(websiteFavicon!!.copy(websiteFavicon.config, true), bitmapPool)
            } else {
                // Draw a placeholder using theme color if it exists, else use a random color.
                val color = if (website.themeColor() != Constants.NO_COLOR) {
                    website.themeColor()
                } else {
                    placeholderColors[Random().nextInt(placeholderColors.size)]
                }
                val createdIcon = createPlaceholderImage(color, website.safeLabel())
                BitmapResource.obtain(createdIcon.copy(createdIcon.config, true), bitmapPool)
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun createPlaceholderImage(@ColorInt color: Int, label: String): Bitmap {
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