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

package arun.com.chromer.util.drawer

import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import arun.com.chromer.util.glide.GlideApp
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader
import javax.inject.Inject

class GlideDrawerImageLoader
@Inject
constructor() : AbstractDrawerImageLoader() {

  override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable) {
    GlideApp.with(imageView).load(uri).placeholder(placeholder).into(imageView)
  }

  override fun cancel(imageView: ImageView) {
    GlideApp.with(imageView).clear(imageView)
  }
}
