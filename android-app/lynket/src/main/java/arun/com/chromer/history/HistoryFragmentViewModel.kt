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

package arun.com.chromer.history

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arun.com.chromer.data.history.HistoryRepository
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.util.SchedulerProvider
import rx.Observable
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by arunk on 14-01-2018.
 */
class HistoryFragmentViewModel
@Inject
constructor(
  private val historyRepository: HistoryRepository
) : ViewModel() {

  val loadingLiveData = MutableLiveData<Boolean>()
  var historyPagedListLiveData = historyRepository.pagedHistory()

  private val loaderSubject: PublishSubject<Int> = PublishSubject.create()
  val subs = CompositeSubscription()

  init {
    /* subs.add(loaderSubject
             .doOnNext { loadingLiveData.postValue(true) }
             .switchMap {
                 historyRepository
                         .allItemsCursor
                         .compose(SchedulerProvider.applyIoSchedulers())
             }.doOnNext { loadingLiveData.postValue(false) }
             .doOnNext(historyCursorLiveData::postValue)
             .subscribe())*/
  }

  fun loadHistory() {
    loaderSubject.onNext(0)
  }

  fun deleteAll(onSuccess: (rows: Int) -> Unit) {
    subs.add(
      historyRepository
        .deleteAll()
        .compose(SchedulerProvider.applyIoSchedulers())
        .doOnNext { rows ->
          loadHistory()
          onSuccess(rows)
        }.subscribe()
    )
  }

  fun deleteHistory(website: Website?) {
    subs.add(Observable.just(website)
      .filter { webSite -> webSite?.url != null }
      .flatMap { historyRepository.delete(it!!) }
      .compose(SchedulerProvider.applyIoSchedulers())
      .doOnError(Timber::e)
      .doOnNext { loadHistory() }
      .subscribe())
  }

  override fun onCleared() {
    subs.clear()
  }
}
