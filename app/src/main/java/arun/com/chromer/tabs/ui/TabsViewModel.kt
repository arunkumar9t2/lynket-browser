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

package arun.com.chromer.tabs.ui

import android.arch.lifecycle.ViewModel
import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.tabs.TabsManager
import rx.Emitter
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject


class TabsViewModel
@Inject
constructor(
        private val tabsManager: DefaultTabsManager,
        private val websiteRepository: WebsiteRepository
) : ViewModel() {

    private val subs = CompositeSubscription()

    fun loadTabs(requester: Observable<Int>): Observable<List<TabsManager.Tab>> {
        return requester.switchMap {
            tabsManager.getActiveTabs()
                    .subscribeOn(Schedulers.io())
                    .toObservable()
                    .flatMap { list ->
                        Observable.create<List<TabsManager.Tab>>({ emitter ->
                            emitter.onNext(list)

                            // For each item, dispatch parallel task to fetch website details.
                            val websiteLoader = Observable.from(list.mapIndexed { index, tab -> Pair(index, tab) })
                                    .onBackpressureBuffer()
                                    .flatMap({ tabPair ->
                                        websiteRepository.getWebsite(tabPair.second.url)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(Schedulers.io())
                                                .doOnNext {
                                                    val index = tabPair.first
                                                    val tab = TabsManager.Tab(tabPair.second.url, tabPair.second.type, it)
                                                    // Set updated Tab to the list.
                                                    (list as MutableList)[index] = tab
                                                    // Emit the updated list.
                                                    emitter.onNext(list)
                                                }
                                    }, 5)
                                    .doOnError(Timber::e)
                                    .doOnCompleted { emitter.onCompleted() }
                                    .subscribe()

                            emitter.setCancellation { websiteLoader.unsubscribe() }

                        }, Emitter.BackpressureMode.LATEST).subscribeOn(Schedulers.io())
                    }
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    override fun onCleared() {
        subs.clear()
    }
}

