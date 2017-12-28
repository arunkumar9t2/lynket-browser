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

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.tabs.TabsManager
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

class TabsViewModel
@Inject
constructor(
        private val tabsManager: DefaultTabsManager,
        private val websiteRepository: WebsiteRepository
) : ViewModel() {
    val loadingLiveData = MutableLiveData<Boolean>()
    val tabsData = MutableLiveData<MutableList<TabsManager.Tab>>()

    fun activeTabs(requester: Observable<Int>): Observable<MutableList<TabsManager.Tab>> {
        return requester
                .doOnNext { loadingLiveData.value = true }
                .switchMap {
                    tabsManager.getActiveTabs()
                            .onErrorReturn { emptyList() }
                            .subscribeOn(Schedulers.io())
                            .toObservable()
                            .concatMapIterable { it }
                            .concatMap { tab ->
                                websiteRepository.getWebsite(tab.url)
                                        .map { website ->
                                            tab.apply {
                                                this.website = website
                                            }
                                        }
                            }.toList()
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext { tabs ->
                                loadingLiveData.value = false
                                tabsData.value = tabs
                            }
                }
    }
}
