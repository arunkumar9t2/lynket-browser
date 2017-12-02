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

package arun.com.chromer.glide.favicon

import arun.com.chromer.data.website.model.WebSite
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

class WebsiteDataFetcher(private val webSite: WebSite) : DataFetcher<WebSite> {

    override fun loadData(priority: Priority?, callback: DataFetcher.DataCallback<in WebSite>) {
        callback.onDataReady(webSite)
    }

    override fun cleanup() {
        // Do nothing.
    }

    override fun cancel() {
        // Do nothing.
    }

    override fun getDataClass(): Class<WebSite> = WebSite::class.java

    override fun getDataSource(): DataSource = DataSource.LOCAL
}