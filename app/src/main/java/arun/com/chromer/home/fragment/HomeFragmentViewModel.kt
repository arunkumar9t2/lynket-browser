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

package arun.com.chromer.home.fragment

import android.arch.lifecycle.ViewModel
import arun.com.chromer.data.Result
import arun.com.chromer.data.history.HistoryRepository
import arun.com.chromer.data.website.model.Website
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * Created by arunk on 14-01-2018.
 */
class HomeFragmentViewModel
@Inject
constructor(
        private val historyRepository: HistoryRepository
) : ViewModel() {
    private val recentsSubject = BehaviorSubject.create<Result<List<Website>>>(Result.Idle())

    private val loaderSubject: PublishSubject<Int> = PublishSubject.create()
    val subs = CompositeSubscription()

    fun loadRecents() {
        loaderSubject.onNext(0)
    }

    fun recentsObservable(): Observable<Result<List<Website>>> {
        if (recentsSubject.value is Result.Idle<List<Website>>) {
            subs.add(loaderSubject.asObservable()
                    .concatMap {
                        historyRepository
                                .recents()
                                .compose(Result.applyToObservable())
                    }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(recentsSubject))
        }
        return recentsSubject.asObservable()
    }

    override fun onCleared() {
        subs.clear()
    }
}
