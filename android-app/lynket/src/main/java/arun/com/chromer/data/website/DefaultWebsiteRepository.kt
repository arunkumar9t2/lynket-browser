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

package arun.com.chromer.data.website

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Pair
import arun.com.chromer.data.common.qualifiers.Disk
import arun.com.chromer.data.common.qualifiers.Network
import arun.com.chromer.data.history.HistoryRepository
import arun.com.chromer.data.website.model.WebColor
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.data.website.stores.WebsiteStore
import arun.com.chromer.shared.Constants.NO_COLOR
import arun.com.chromer.util.SchedulerProvider
import rx.Observable
import rx.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Website repository implementation for managing and providing website data.
 */
@Singleton
class DefaultWebsiteRepository
@Inject
internal constructor(
  @param:Disk private val cacheStore: WebsiteStore,
  @param:Network private val webNetworkStore: WebsiteStore,
  private val historyRepository: HistoryRepository
) : WebsiteRepository {

  override fun getWebsite(url: String): Observable<Website> {
    val cache = cacheStore.getWebsite(url)
      .doOnNext { webSite ->
        if (webSite != null) {
          historyRepository.insert(webSite).subscribe()
        }
      }

    val history = historyRepository.get(Website(url))
      .doOnNext { webSite ->
        if (webSite != null) {
          historyRepository.insert(webSite).subscribe()
        }
      }


    val remote = webNetworkStore.getWebsite(url)
      .filter { webSite -> webSite != null }
      .doOnNext { webSite ->
        cacheStore.saveWebsite(webSite).subscribe()
        historyRepository.insert(webSite).subscribe()
      }


    return Observable.concat(cache, history, remote)
      .first { webSite -> webSite != null }
      .doOnError { Timber.e(it) }
      .onErrorReturn { throwable ->
        Timber.e(throwable)
        Website(url)
      }.compose(SchedulerProvider.applyIoSchedulers())
  }


  override fun getWebsiteReadOnly(url: String): Observable<Website> {
    val cache = cacheStore.getWebsite(url)
    val history = historyRepository.get(Website(url))
    val remote = webNetworkStore.getWebsite(url)
      .filter { webSite -> webSite != null }
      .doOnNext { webSite -> cacheStore.saveWebsite(webSite).subscribe() }
    return Observable.concat(cache, history, remote)
      .first { webSite -> webSite != null }
      .doOnError { Timber.e(it) }
      .onErrorReturn { throwable ->
        Timber.e(throwable)
        Website(url)
      }.compose(SchedulerProvider.applyIoSchedulers())
  }

  override fun getWebsiteColorSync(url: String): Int {
    return cacheStore.getWebsiteColor(url)
      .map { webColor ->
        if (webColor.color == NO_COLOR) {
          saveWebColor(url).subscribe()
        }
        webColor
      }.toBlocking()
      .first()
      .color
  }

  override fun saveWebColor(url: String): Observable<WebColor> {
    return getWebsiteReadOnly(url)
      .observeOn(Schedulers.io())
      .flatMap { webSite ->
        if (webSite != null) {
          if (webSite.themeColor() != NO_COLOR) {
            val color = webSite.themeColor()
            cacheStore.saveWebsiteColor(Uri.parse(webSite.url).host!!, color)
          } else {
            val color = getWebsiteIconAndColor(webSite).second
            if (color != NO_COLOR) {
              cacheStore.saveWebsiteColor(Uri.parse(webSite.url).host!!, color)
            } else Observable.empty()
          }
        } else Observable.empty()
      }
  }

  override fun clearCache(): Observable<Void> = cacheStore.clearCache()

  override fun getWebsiteIconAndColor(website: Website): Pair<Bitmap, Int> {
    return webNetworkStore.getWebsiteIconAndColor(website)
  }

  override fun getWebsiteRoundIconAndColor(website: Website): Pair<Drawable, Int> {
    return webNetworkStore.getWebsiteRoundIconAndColor(website)
  }

  override fun getWebsiteIconWithPlaceholderAndColor(website: Website): Pair<Bitmap, Int> {
    return webNetworkStore.getWebsiteIconWithPlaceholderAndColor(website)
  }
}
