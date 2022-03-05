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

package arun.com.chromer.tabs.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.tabs.TabsManager
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject

class TabsViewModel
@Inject
constructor(
  private val tabsManager: TabsManager,
  private val websiteRepository: WebsiteRepository
) : ViewModel() {
  val loadingLiveData = MutableLiveData<Boolean>()
  val tabsData = MutableLiveData<MutableList<TabsManager.Tab>>()

  private val loaderSubject: PublishSubject<Int> = PublishSubject.create()
  val subs = CompositeSubscription()

  init {
    subs.add(loaderSubject
      .asObservable()
      .doOnNext { loadingLiveData.value = true }
      .switchMap { _ ->
        tabsManager.getActiveTabs()
          .onErrorReturn { emptyList() }
          .subscribeOn(Schedulers.io())
          .toObservable()
          .concatMapIterable { it }
          .concatMap { tab ->
            websiteRepository.getWebsiteReadOnly(tab.url)
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
      }.subscribe()
    )
  }


  fun loadTabs() {
    loaderSubject.onNext(0)
  }

  fun clearAllTabs() {
    subs.add(tabsManager.closeAllTabs()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSuccess { loadTabs() }
      .doOnError(Timber::e)
      .subscribe())
  }

  override fun onCleared() {
    subs.clear()
  }
}
