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

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

class ApplicationIconDataFetcher(private val packageName: String) : DataFetcher<ApplicationIcon> {

  override fun loadData(
    priority: Priority,
    callback: DataFetcher.DataCallback<in ApplicationIcon>
  ) {
    callback.onDataReady(ApplicationIcon(packageName))
  }

  override fun cleanup() {
    // Do nothing.
  }

  override fun cancel() {
    // Do nothing.
  }

  override fun getDataClass(): Class<ApplicationIcon> = ApplicationIcon::class.java

  override fun getDataSource(): DataSource = DataSource.LOCAL
}
