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

import android.net.Uri
import arun.com.chromer.glide.appicon.ApplicationIcon.Companion.URI_SCHEME_APPLICATION_ICON
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class ApplicationIconModelLoader : ModelLoader<Any, ApplicationIcon> {

    override fun buildLoadData(model: Any, width: Int, height: Int, options: Options?): LoadData<ApplicationIcon>? {
        val uriStr: String
        val uri: Uri

        when (model) {
            is String -> {
                uriStr = model
                uri = Uri.parse(uriStr)
            }
            is Uri -> {
                uri = model
                uriStr = uri.toString()
            }
            else -> throw IllegalStateException("model must either be Uri or String.")
        }
        val packageName = uri.schemeSpecificPart
        return LoadData(ObjectKey(uriStr), ApplicationIconDataFetcher(packageName))
    }

    override fun handles(model: Any): Boolean =
            when (model) {
                is String -> Uri.parse(model).scheme == URI_SCHEME_APPLICATION_ICON
                is Uri -> model.scheme == URI_SCHEME_APPLICATION_ICON
                else -> false
            }


    class Factory : ModelLoaderFactory<Any, ApplicationIcon> {

        override fun build(multiFactory: MultiModelLoaderFactory?): ModelLoader<Any, ApplicationIcon> = ApplicationIconModelLoader()

        override fun teardown() {
            // Do nothing.
        }
    }
}