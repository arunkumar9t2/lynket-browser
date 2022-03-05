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

package arun.com.chromer.browsing.providerselection

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arun.com.chromer.data.apps.AppRepository
import arun.com.chromer.data.apps.model.Provider
import arun.com.chromer.util.SchedulerProvider
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by arunk on 17-02-2018.
 */
class ProviderSelectionViewModel
@Inject
constructor(
  private val appRepository: AppRepository
) : ViewModel() {
  val subs = CompositeSubscription()

  private val loadingQueue = PublishSubject.create<Int>()

  val providersLiveData = MutableLiveData<List<Provider>>()

  init {
    subs.add(loadingQueue.asObservable()
      .switchMap {
        return@switchMap appRepository
          .allProviders()
          .compose(SchedulerProvider.applyIoSchedulers())
      }.subscribe({ providers ->
        providersLiveData.value = providers
      }, Timber::e)
    )
  }

  fun loadProviders() {
    loadingQueue.onNext(0)
  }

  override fun onCleared() {
    subs.clear()
  }
}
