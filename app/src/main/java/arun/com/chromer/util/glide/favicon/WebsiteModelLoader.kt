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

package arun.com.chromer.util.glide.favicon

import arun.com.chromer.data.website.model.Website
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class WebsiteModelLoader : ModelLoader<Any, Website> {

    override fun buildLoadData(model: Any, width: Int, height: Int, options: Options?): LoadData<Website>? {
        return when (model) {
            is Website -> LoadData(ObjectKey(model), WebsiteDataFetcher(website = model))
            else -> throw IllegalStateException("model is not Website")
        }
    }

    override fun handles(model: Any): Boolean = model is Website

    class Factory : ModelLoaderFactory<Any, Website> {

        override fun build(multiFactory: MultiModelLoaderFactory?): ModelLoader<Any, Website> = WebsiteModelLoader()

        override fun teardown() {
            // Do nothing.
        }
    }
}