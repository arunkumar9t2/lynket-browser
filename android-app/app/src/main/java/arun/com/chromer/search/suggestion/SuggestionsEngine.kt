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

package arun.com.chromer.search.suggestion

import `in`.arunkumarsampath.suggestions.RxSuggestions
import android.app.Application
import arun.com.chromer.data.history.HistoryRepository
import arun.com.chromer.search.suggestion.items.CopySuggestionItem
import arun.com.chromer.search.suggestion.items.GoogleSuggestionItem
import arun.com.chromer.search.suggestion.items.HistorySuggestionItem
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.util.Utils
import rx.Observable
import rx.Observable.Transformer
import rx.Observable.just
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

/**
 * Helper class that collates suggestions from multiple sources and publishes them in a single stream.
 */
@Singleton
class SuggestionsEngine @Inject
constructor(private var application: Application, private val historyRepository: HistoryRepository) {
    private val suggestionsDebounce = 200L
    private val suggestionsLimit = 5

    /**
     * Trims and filters empty strings in stream.
     */
    private fun emptyStringFilter(): Transformer<String, String> {
        return Transformer { stringObservable ->
            stringObservable
                    .filter { s -> s != null }
                    .map { it.trim { query -> query <= ' ' } }
                    .filter { s -> !s.isEmpty() }
        }
    }

    /**
     * Converts a stream of strings into stream of list of suggestion items collated from device'c
     * clipboard, history and google suggestions.
     */
    fun suggestionsTransformer(): Transformer<String, List<SuggestionItem>> {
        return Transformer { suggestion ->
            val deviceSuggestions = just(listOf(CopySuggestionItem(application)))
            return@Transformer suggestion
                    .observeOn(Schedulers.io())
                    .compose(emptyStringFilter())
                    .switchMap { query ->
                        val googleSuggestions = just(query).compose(googleTransformer())
                        val historySuggestions = just(query).compose(historyTransformer())

                        return@switchMap Observable.zip(
                                googleSuggestions,
                                historySuggestions,
                                deviceSuggestions
                        ) { googleList, historyList, deviceList ->
                            val copyItem = deviceList[0]
                            val suggestions = ArrayList<SuggestionItem>()

                            // Add copy item if it is valid
                            if (!copyItem.title.isEmpty()) {
                                suggestions.add(copyItem)
                            }
                            // Add all Google suggestions
                            suggestions.addAll(googleList)

                            // Based on current length, figure out an index which we can use to fill history items.
                            val currentLength = Math.min(suggestions.size, suggestionsLimit)
                            var insertIndex = Math.min(Math.max(suggestionsLimit - 2, currentLength - historyList.size), currentLength)
                            val historyMutable = historyList.toMutableList()

                            while (insertIndex < suggestionsLimit && historyMutable.isNotEmpty()) {
                                if (insertIndex >= suggestions.size) {
                                    suggestions.add(insertIndex, historyMutable.removeAt(0))
                                } else {
                                    suggestions[insertIndex] = historyMutable.removeAt(0)
                                }
                                insertIndex++
                            }
                            suggestions.take(suggestionsLimit)
                        }.onErrorReturn { emptyList() }
                    }
        }
    }


    /**
     * Fetches suggestions from Google and converts it to {@link GoogleSuggestionItem}
     */
    private fun googleTransformer(): Transformer<String, List<SuggestionItem>> {
        return Transformer { query ->
            if (!Utils.isOnline(application)) {
                return@Transformer just(emptyList())
            } else return@Transformer query
                    .compose(RxSuggestions.suggestionsTransformer(suggestionsLimit))
                    .map { it.map { query -> GoogleSuggestionItem(query) } as List<SuggestionItem> }
                    .onErrorReturn { Collections.emptyList() }
        }
    }


    /**
     * Fetches matching items from History database and converts them to list of suggestions.
     */
    private fun historyTransformer(): Transformer<String, List<SuggestionItem>> {
        return Transformer { query ->
            return@Transformer query
                    .debounce(suggestionsDebounce, TimeUnit.MILLISECONDS)
                    .switchMap { historyRepository.search(it) }
                    .map {
                        it.asSequence()
                                .map { query -> HistorySuggestionItem(query) }
                                .take(suggestionsLimit)
                                .toList() as List<SuggestionItem>
                    }.onErrorReturn { emptyList() }
        }
    }

}
