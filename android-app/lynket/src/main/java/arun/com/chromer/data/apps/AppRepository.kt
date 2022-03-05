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

package arun.com.chromer.data.apps

import androidx.annotation.ColorInt
import arun.com.chromer.data.apps.model.Provider

import arun.com.chromer.data.common.App
import rx.Observable

interface AppRepository {
  fun getApp(packageName: String): Observable<App>

  fun saveApp(app: App): Observable<App>

  fun isPackageBlacklisted(packageName: String): Boolean

  fun setPackageBlacklisted(packageName: String): Observable<App>

  fun isPackageIncognito(packageName: String): Boolean

  fun setPackageIncognito(packageName: String): Observable<App>

  fun removeIncognito(packageName: String): Observable<App>

  @ColorInt
  fun getPackageColorSync(packageName: String): Int

  fun getPackageColor(packageName: String): Observable<Int>

  fun setPackageColor(packageName: String, color: Int): Observable<App>

  fun removeBlacklist(packageName: String): Observable<App>

  fun allApps(): Observable<List<App>>

  fun allProviders(): Observable<List<Provider>>
}
