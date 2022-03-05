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

package arun.com.chromer.extenstions

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

/**
 * Created by arunk on 01-12-2017.
 */
fun Drawable.toBitmap(): Bitmap {
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

/**
 * Applies given @param color on a new [Drawable] and returns it.
 */
fun Drawable.applyColor(@ColorInt color: Int): Drawable =
  this.mutate().apply { setColorFilter(color, PorterDuff.Mode.SRC_ATOP) }
