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

package arun.com.chromer.glide.appicon

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource

class ApplicationIconDecoder(private val context: Context, glide: Glide) : ResourceDecoder<ApplicationIcon, Bitmap> {

    private val bitmapPool: BitmapPool = glide.bitmapPool

    override fun handles(source: ApplicationIcon, options: Options?): Boolean = true

    override fun decode(source: ApplicationIcon, with: Int, height: Int, options: Options?): Resource<Bitmap>? {
        val packageName = source.packageName
        val packageManager = context.packageManager
        return try {
            packageManager.getApplicationIcon(packageName).bitmap.let { bitmap ->
                val bitmapCopy = bitmap.copy(bitmap.config, true)
                BitmapResource.obtain(bitmapCopy, bitmapPool)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private val Drawable.bitmap: Bitmap
        get() {
            if (this is BitmapDrawable && bitmap != null) {
                return bitmap
            }

            val bitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
                // Single color bitmap will be created of 1x1 pixel.
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            }
            val canvas = Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            return bitmap
        }
}