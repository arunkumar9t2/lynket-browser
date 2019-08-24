/*
 * Lynket
 *
 * Copyright (C) 2019 Arunkumar
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

package arun.com.chromer.search.view

import android.annotation.SuppressLint
import arun.com.chromer.di.scopes.PerView
import arun.com.chromer.di.view.Detaches
import arun.com.chromer.search.suggestion.SuggestionsEngine
import arun.com.chromer.search.suggestion.items.SuggestionItem
import com.jakewharton.rxrelay2.BehaviorRelay
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import hu.akarnokd.rxjava.interop.RxJavaInterop
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by arunk on 12-12-2017.
 */
@SuppressLint("CheckResult")
interface Search {
    @PerView
    class SearchPresenter
    @Inject
    constructor(
            private val suggestionsEngine: SuggestionsEngine,
            private val schedulerProvider: SchedulerProvider,
            @param:Detaches
            private val detaches: Observable<Unit>
    ) {
        private val suggestionsSubject = BehaviorRelay.create<List<SuggestionItem>>()
        val suggestions: Observable<List<SuggestionItem>> = suggestionsSubject.hide()

        fun registerSearch(queryObservable: Observable<String>) {
            queryObservable
                    .toFlowable(BackpressureStrategy.LATEST)
                    .compose(RxJavaInterop.toV2Transformer(suggestionsEngine.suggestionsTransformer()))
                    .toObservable()
                    .doOnError(Timber::e)
                    .takeUntil(detaches)
                    .subscribe { suggestionsSubject.accept(it) }
        }

        fun cleanUp() {
        }
    }
}