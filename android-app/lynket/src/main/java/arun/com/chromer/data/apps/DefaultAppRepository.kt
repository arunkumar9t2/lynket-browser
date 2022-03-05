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

import android.app.Application
import android.content.Intent
import arun.com.chromer.browsing.customtabs.dynamictoolbar.AppColorExtractorJob
import arun.com.chromer.data.apps.qualifiers.System
import arun.com.chromer.data.apps.store.AppStore
import arun.com.chromer.data.common.App
import arun.com.chromer.data.common.qualifiers.Disk
import arun.com.chromer.shared.Constants
import rx.Observable
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAppRepository
@Inject internal constructor(
  private val application: Application,
  @param:Disk private val diskStore: AppStore,
  @param:System private val systemStore: AppStore
) : AppRepository {

  override fun getApp(packageName: String): Observable<App> {
    return diskStore.getApp(packageName)
  }

  override fun saveApp(app: App): Observable<App> {
    return diskStore.saveApp(app)
  }

  override fun getPackageColor(packageName: String): Observable<Int> {
    return diskStore.getPackageColor(packageName)
      .doOnNext { integer ->
        if (integer == Constants.NO_COLOR) {
          Timber.d("Color not found, starting extraction.")
          AppColorExtractorJob.enqueueWork(
            application,
            AppColorExtractorJob::class.java,
            AppColorExtractorJob.JOB_ID,
            Intent().putExtra(Constants.EXTRA_PACKAGE_NAME, packageName)
          )
        }
      }
  }

  override fun setPackageColor(packageName: String, color: Int): Observable<App> {
    return diskStore.setPackageColor(packageName, color)
  }

  override fun removeBlacklist(packageName: String): Observable<App> {
    return diskStore.removeBlacklist(packageName)
  }

  override fun isPackageBlacklisted(packageName: String): Boolean {
    return diskStore.isPackageBlacklisted(packageName)
  }

  override fun setPackageBlacklisted(packageName: String): Observable<App> {
    return diskStore.setPackageBlacklisted(packageName)
  }

  override fun getPackageColorSync(packageName: String): Int {
    return getPackageColor(packageName).toBlocking().first()
  }

  override fun isPackageIncognito(packageName: String): Boolean {
    return diskStore.isPackageIncognito(packageName)
  }

  override fun setPackageIncognito(packageName: String): Observable<App> {
    return diskStore.setPackageIncognito(packageName)
  }

  override fun removeIncognito(packageName: String): Observable<App> {
    return diskStore.removeIncognito(packageName)
  }

  override fun allApps(): Observable<List<App>> {
    val appComparator = App.PerAppListComparator()
    return systemStore.getInstalledApps()
      .map { app ->
        app.blackListed = diskStore.isPackageBlacklisted(app.packageName)
        app.incognito = diskStore.isPackageIncognito(app.packageName)
        app
      }
      .toSortedList { app1, app2 -> appComparator.compare(app1, app2) }
  }

  override fun allProviders() = systemStore.allProviders()
}
