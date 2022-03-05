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

package arun.com.chromer.home

import androidx.lifecycle.LifecycleOwner
import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.tabs.TabsManager
import arun.com.chromer.util.lifecycle.ActivityLifecycle
import arun.com.chromer.util.lifecycle.LifecycleEvents
import dev.arunkumar.android.dagger.activity.PerActivity
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Observable
import hu.akarnokd.rxjava.interop.RxJavaInterop.toV2Single
import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@PerActivity
class TabsLifecycleObserver
@Inject
constructor(
  @ActivityLifecycle
  lifecycleOwner: LifecycleOwner,
  private val tabsManager: TabsManager,
  private val schedulerProvider: SchedulerProvider,
  private val websiteRepository: WebsiteRepository
) : LifecycleEvents(lifecycleOwner) {
  fun activeTabs(): Observable<List<TabsManager.Tab>> = starts.flatMap {
    Observable.interval(750, TimeUnit.MILLISECONDS)
      .flatMapSingle { toV2Single(tabsManager.getActiveTabs()) }
      .startWith(toV2Single(tabsManager.getActiveTabs()).toObservable())
      .distinctUntilChanged()
      .switchMap { tabs ->
        tabs.toObservable()
          .flatMap { tab ->
            toV2Observable(websiteRepository.getWebsiteReadOnly(tab.url)
              .map { website -> tab.copy(website = website) })
              .sorted { o1, o2 ->
                val createdAt = o1.website?.createdAt ?: 0
                val createdAt2 = o2.website?.createdAt ?: 0
                createdAt.compareTo(createdAt2)
              }
          }.toList()
          .toObservable()
          .startWith(tabs)
          .debounce(200, TimeUnit.MILLISECONDS)
      }
      .takeUntil(stops)
      .compose(schedulerProvider.poolToUi())
  }
}
