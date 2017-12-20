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

import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.di.scopes.PerFragment
import arun.com.chromer.shared.base.Base
import arun.com.chromer.tabs.DefaultTabsManager
import arun.com.chromer.tabs.TabsManager
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by arunk on 20-12-2017.
 */
interface Tabs {
    interface View : Base.View {
        fun setTabs(tabs: List<TabsManager.Tab>)
        fun setTab(index: Int, tab: TabsManager.Tab)
    }

    @PerFragment
    class Presenter
    @Inject
    constructor(
            private val tabsManager: DefaultTabsManager,
            private val websiteRepository: WebsiteRepository
    ) : Base.Presenter<View>() {

        fun register(requester: Observable<Int>) {
            subs.add(requester.switchMap {
                tabsManager.getActiveTabs()
                        .onErrorReturn { emptyList() }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSuccess {
                            if (isViewAttached) {
                                view.setTabs(it)
                                Timber.d(it.toString())
                            }
                        }
                        .observeOn(Schedulers.io())
                        .toObservable()
                        .map { it.mapIndexed { index, tab -> Pair(index, tab) } }
                        .flatMapIterable { it }
                        .concatMap {
                            val index = it.first
                            val tab = it.second
                            websiteRepository.getWebsite(tab.url)
                                    .map {
                                        tab.website = it
                                        Pair(index, tab)
                                    }
                        }.observeOn(AndroidSchedulers.mainThread())
                        .doOnNext {
                            if (isViewAttached) {
                                view.setTab(it.first, it.second)
                            }
                        }
            }.subscribe())
        }
    }
}