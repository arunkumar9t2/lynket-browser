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

import android.net.Uri
import arun.com.chromer.util.glide.appicon.ApplicationIcon.Companion.URI_SCHEME_APPLICATION_ICON
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class ApplicationIconModelLoader : ModelLoader<Uri, ApplicationIcon> {

  override fun buildLoadData(
    model: Uri,
    width: Int,
    height: Int,
    options: Options
  ) = LoadData(ObjectKey(model), ApplicationIconDataFetcher(model.schemeSpecificPart))

  override fun handles(model: Uri): Boolean = model.scheme == URI_SCHEME_APPLICATION_ICON

  class Factory : ModelLoaderFactory<Uri, ApplicationIcon> {

    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Uri, ApplicationIcon> =
      ApplicationIconModelLoader()

    override fun teardown() {
      // Do nothing.
    }
  }
}
