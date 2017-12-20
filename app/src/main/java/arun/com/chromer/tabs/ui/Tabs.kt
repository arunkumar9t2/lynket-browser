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
import javax.inject.Inject

/**
 * Created by arunk on 20-12-2017.
 */
interface Tabs {
    interface View : Base.View {
        fun loading(loading: Boolean)
        fun setTabs(tabs: List<TabsManager.Tab>)
    }

    @PerFragment
    class Presenter
    @Inject
    constructor(
            private val tabsManager: DefaultTabsManager,
            private val websiteRepository: WebsiteRepository
    ) : Base.Presenter<View>() {

        fun register(requester: Observable<Int>) {
            subs.add(requester
                    .doOnNext { loading() }
                    .switchMap {
                        tabsManager.getActiveTabs()
                                .onErrorReturn { emptyList() }
                                .subscribeOn(Schedulers.io())
                                .toObservable()
                                .concatMapIterable { it }
                                .concatMap { tab ->
                                    websiteRepository.getWebsite(tab.url).map { website ->
                                        tab.website = website
                                        tab
                                    }
                                }.toList()
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnError { stopLoading() }
                                .doOnNext {
                                    if (isViewAttached) {
                                        view.loading(false)
                                        view.setTabs(it)
                                    }
                                }
                    }.subscribe())
        }

        private fun stopLoading() {
            if (isViewAttached) {
                view.loading(false)
            }
        }

        private fun loading() {
            if (isViewAttached) {
                view.loading(true)
            }
        }
    }
}