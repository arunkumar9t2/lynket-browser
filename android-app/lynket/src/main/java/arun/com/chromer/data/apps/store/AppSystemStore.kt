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

package arun.com.chromer.data.apps.store

import android.app.Application
import android.content.Intent
import android.content.pm.ResolveInfo
import arun.com.chromer.browsing.customtabs.CustomTabs.getCustomTabSupportingPackages
import arun.com.chromer.data.apps.model.Provider
import arun.com.chromer.data.common.App
import arun.com.chromer.extenstions.toUri
import arun.com.chromer.shared.Constants
import arun.com.chromer.util.Utils
import arun.com.chromer.util.Utils.getAppNameWithPackage
import arun.com.chromer.util.glide.appicon.ApplicationIcon.Companion.createUri
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by arunk on 10-02-2018.
 */
@Singleton
class AppSystemStore
@Inject
constructor(
  private val application: Application
) : AppStore {

  private val allProviders: List<Provider> by lazy {
    arrayListOf(
      Provider(
        "com.android.chrome",
        "Google Chrome",
        "https://lh3.googleusercontent.com/nYhPnY2I-e9rpqnid9u9aAODz4C04OycEGxqHG5vxFnA35OGmLMrrUmhM9eaHKJ7liB-=w300-rw".toUri(),
        features = """
                                Latest from Google
                                Data saver, form auto fill, Google account cloud sync
                                Bottom Bar
                                Web heads
                                Aggressive background loading
                                Action button
                            """.trimIndent()
      ),
      Provider(
        "com.brave.browser",
        "Brave Browser",
        "https://lh3.googleusercontent.com/v8h4MEDGbDtwKD13-38Jqwh7UgHU7XJ76DSp3yzyF99mwQTDoM8wgDg7apwMulpQgopG=w300-rw".toUri(),
        features = """
                                Ad block
                                Tracking protection
                                Bottom Bar
                                Web heads
                                Aggressive background loading
                                Action button
                            """.trimIndent()
      ),
      Provider(
        "org.mozilla.focus",
        "Firefox Focus",
        "https://lh3.googleusercontent.com/uoqToM7l-x3lZNjFOzNkVxEilkKfEzGh9v8BB8b6pP1l9TltE4Sxd1XGJuiksjM4a1s=w300-rw".toUri(),
        features = """
                                Tracking protection
                                Always incognito
                                Web heads
                                Action button
                            """.trimIndent()
      ),
      Provider(
        "com.sec.android.app.sbrowser",
        "Samsung Internet",
        "https://lh3.googleusercontent.com/Z2DsZU3fKSSluPHNS-6CoLk29POTX-kBYtRlkRrbvyfQZEEBLH0j8DEkwbEH4nhW6E-e=w300-rw".toUri(),
        features = """
                                Samsung Cloud and Samsung Pass
                                Ad block
                                Tracking protection
                                Bottom Bar
                                Web heads
                                Aggressive background loading
                                Night mode
                            """.trimIndent()
      ),
      Provider(
        "org.mozilla.fennec_aurora",
        "Firefox Nightly",
        "https://lh3.googleusercontent.com/5ZYLS3ztW1XBfSf32onyhAVLq_uZQmJIYdhz8VlQwuvpB7x73jaDqtJlTtmxcsvit0I=w300-rw".toUri(),
        features = """
                                Firefox account sync
                                Ad block
                                Tracking protection
                                Web heads
                                Aggressive background loading
                                Action button
                            """.trimIndent()
      ),
      Provider(
        "com.kiwibrowser.browser",
        "Kiwi Browser",
        "https://lh3.googleusercontent.com/IpPy16lik1fLrJs0fkaFuKrUm6Hw9Q3KDa2gLbewoze0Ko39gEIOyDECYOZBFJLHGeo=s300-rw".toUri(),
        features = """
                                Dark mode
                                Ad block
                                Tracking protection
                                Web heads
                                Bottom Bar
                                Aggressive background loading
                                Extensions support
                            """.trimIndent()
      ),
      Provider(
        "com.vivaldi.browser",
        "Vivaldi Browser",
        "https://lh3.googleusercontent.com/xL5ky9AeFvI5VC6jRPKJf_0fMj0NKJYKRWc0wkl_5LFy9BCWc1OauInLmSfFP8soJLkg=s300-rw".toUri(),
        features = """
                                Dark mode
                                Web heads
                                Aggressive background loading
                                Speed Dial
                                Bottom Bar
                                Desktop Sync
                            """.trimIndent()
      )
    )
  }

  override fun getApp(packageName: String): Observable<App> = Observable.empty()

  override fun saveApp(app: App): Observable<App> = Observable.empty()

  override fun isPackageBlacklisted(packageName: String): Boolean = false

  override fun setPackageBlacklisted(packageName: String): Observable<App> = Observable.empty()

  override fun isPackageIncognito(packageName: String): Boolean = false

  override fun setPackageIncognito(packageName: String): Observable<App> = Observable.empty()

  override fun removeIncognito(packageName: String): Observable<App> = Observable.empty()

  override fun getPackageColorSync(packageName: String): Int = Constants.NO_COLOR

  override fun getPackageColor(packageName: String): Observable<Int> =
    Observable.just(Constants.NO_COLOR)

  override fun setPackageColor(packageName: String, color: Int): Observable<App> =
    Observable.empty()

  override fun removeBlacklist(packageName: String): Observable<App> = Observable.empty()

  override fun getInstalledApps(): Observable<App> {
    val pm = application.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return Observable.fromCallable<List<ResolveInfo>> { pm.queryIntentActivities(intent, 0) }
      .flatMapIterable { resolveInfos -> resolveInfos }
      .filter { resolveInfo ->
        resolveInfo != null && !resolveInfo.activityInfo.packageName.equals(
          application.packageName,
          ignoreCase = true
        )
      }
      .map { resolveInfo ->
        Utils.createApp(application, resolveInfo.activityInfo.packageName)
      }.distinct { it.packageName }
  }

  override fun allProviders(): Observable<List<Provider>> {
    val preLoadedProviders = Observable.from(allProviders).map { provider ->
      if (Utils.isPackageInstalled(application, provider.packageName)) {
        provider.iconUri = createUri(provider.packageName)
        provider.appName = getAppNameWithPackage(application, provider.packageName)
        provider.installed = true
      }
      return@map provider
    }

    val installedProviders = Observable.defer {
      Observable.from(getCustomTabSupportingPackages(application).map { packageName ->
        Provider(
          packageName,
          getAppNameWithPackage(application, packageName),
          createUri(packageName)
        ).apply { installed = true }
      })
    }
    return Observable.concat(preLoadedProviders, installedProviders)
      .distinct { it.packageName }
      .toSortedList { t1, t2 -> compareValues(t1.appName, t2.appName) }
  }
}

