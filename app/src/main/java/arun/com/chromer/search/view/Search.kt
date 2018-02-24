/*
 * Lynket
 *
 * Copyright (C) 2018 Arunkumar
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

import arun.com.chromer.di.scopes.PerView
import arun.com.chromer.search.suggestion.SuggestionsEngine
import arun.com.chromer.search.suggestion.items.SuggestionItem
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.lang.ref.WeakReference
import javax.inject.Inject

/**
 * Created by arunk on 12-12-2017.
 */
interface Search {
    interface View {
        fun setSuggestions(suggestionItems: List<SuggestionItem>)
    }

    @PerView
    class Presenter @Inject
    constructor(private val suggestionsEngine: SuggestionsEngine) {
        private var viewRef: WeakReference<Search.View>? = null

        internal var view: Search.View? = null
            get() = viewRef?.get()

        private val subs = CompositeSubscription()

        fun registerSearch(queryObservable: Observable<String>) {
            subs.add(queryObservable
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .compose(suggestionsEngine.suggestionsTransformer())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { suggestionItems -> view?.setSuggestions(suggestionItems) }
                    .doOnError(Timber::e)
                    .subscribe())
        }

        fun takeView(view: Search.View) {
            Timber.d("Took view $view")
            viewRef?.clear()
            viewRef = WeakReference(view)
        }

        fun detachView() {
            Timber.d("View detached")
            viewRef?.clear()
            viewRef = null
        }

        fun cleanUp() {
            detachView()
            subs.clear()
        }
    }
}