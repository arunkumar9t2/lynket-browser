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

package arun.com.chromer.activities.main.home

import arun.com.chromer.data.history.BaseHistoryRepository
import arun.com.chromer.data.website.model.WebSite
import arun.com.chromer.di.PerFragment
import arun.com.chromer.search.SuggestionsEngine
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.shared.common.Base
import arun.com.chromer.shared.common.Snackable
import arun.com.chromer.util.RxUtils
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import javax.inject.Inject

internal interface Home {
    interface View : Snackable, Base.View {
        fun setSuggestions(suggestions: List<SuggestionItem>)

        fun setRecents(webSites: List<WebSite>)
    }

    @PerFragment
    class Presenter @Inject
    constructor(
            private val historyRepository: BaseHistoryRepository,
            private val suggestionsEngine: SuggestionsEngine
    ) : Base.Presenter<View>() {

        fun registerSearch(stringObservable: Observable<String>) {
            subs.add(stringObservable
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .compose(suggestionsEngine.suggestionsTransformer())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { suggestionItems ->
                        if (isViewAttached) {
                            view.setSuggestions(suggestionItems)
                        }
                    }.doOnError(Timber::e)
                    .subscribe())
        }

        fun loadRecents() {
            subs.add(historyRepository.recents()
                    .compose(RxUtils.applySchedulers())
                    .doOnError(Timber::e)
                    .doOnNext { webSites ->
                        if (isViewAttached) {
                            view.setRecents(webSites)
                        }
                    }.subscribe())
        }

        override fun onResume() {

        }

        override fun onPause() {

        }
    }
}
