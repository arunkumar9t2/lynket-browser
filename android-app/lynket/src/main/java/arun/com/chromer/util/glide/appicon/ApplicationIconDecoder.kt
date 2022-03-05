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

package arun.com.chromer.util.glide.appicon

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import arun.com.chromer.extenstions.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource

class ApplicationIconDecoder(
  private val context: Context,
  glide: Glide
) : ResourceDecoder<ApplicationIcon, Bitmap> {
  private val bitmapPool: BitmapPool = glide.bitmapPool

  override fun handles(source: ApplicationIcon, options: Options): Boolean = true

  override fun decode(
    source: ApplicationIcon,
    with: Int,
    height: Int,
    options: Options
  ) = try {
    val packageName = source.packageName
    val packageManager = context.packageManager
    packageManager.getApplicationIcon(packageName).toBitmap().let { bitmap ->
      val bitmapCopy = bitmap.copy(bitmap.config, true)
      BitmapResource.obtain(bitmapCopy, bitmapPool)
    }
  } catch (e: PackageManager.NameNotFoundException) {
    null
  }
}

