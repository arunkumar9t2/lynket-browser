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
import arun.com.chromer.R
import arun.com.chromer.data.history.HistoryRepository
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.search.suggestion.items.SuggestionItem.*
import arun.com.chromer.search.suggestion.items.SuggestionType
import arun.com.chromer.search.suggestion.items.SuggestionType.*
import arun.com.chromer.util.Utils
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import hu.akarnokd.rxjava.interop.RxJavaInterop
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class that collates suggestions from multiple sources and publishes them in a single stream.
 */
@Singleton
class SuggestionsEngine
@Inject
constructor(
        private var application: Application,
        private val historyRepository: HistoryRepository,
        private val schedulerProvider: SchedulerProvider
) {
    private val suggestionsDebounce = 200L
    private val suggestionsLimit = 12

    /**
     * Trims and filters empty strings in stream.
     */
    private fun emptyStringFilter(): FlowableTransformer<String, String> {
        return FlowableTransformer { stringObservable ->
            stringObservable
                    .map { it.trim { query -> query <= ' ' } }
                    .filter { s -> s.isNotEmpty() }
        }
    }

    private fun deviceSuggestions() = Flowable
            .fromCallable {
                Utils.getClipBoardText(application) ?: ""
            }.subscribeOn(schedulerProvider.ui)
            .subscribeOn(schedulerProvider.pool)
            .map<List<SuggestionItem>> { copiedText ->
                if (copiedText.isEmpty()) {
                    emptyList()
                } else {
                    val copySuggestionItem = CopySuggestionItem(
                            copiedText,
                            application.getString(R.string.text_you_copied)
                    )
                    mutableListOf<SuggestionItem>(copySuggestionItem).apply {
                        addAll(Utils.findURLs(copiedText)
                                .map { link ->
                                    CopySuggestionItem(
                                            link,
                                            application.getString(R.string.link_you_copied)
                                    )
                                })
                    }
                }
            }

    /**
     * Converts a stream of strings into stream of list of suggestions items collated from device'c
     * clipboard, history and google suggestions.
     */
    fun suggestionsTransformer(): FlowableTransformer<String, Pair<SuggestionType, List<SuggestionItem>>> {
        return FlowableTransformer { suggestion ->
            suggestion
                    .observeOn(schedulerProvider.pool)
                    .compose(emptyStringFilter())
                    .switchMap { query ->
                        val deviceSuggestions = deviceSuggestions().map { COPY to it }
                        val googleSuggestions = Flowable.just(query)
                                .compose(googleTransformer())
                                .map { GOOGLE to it }
                        val historySuggestions = Flowable.just(query)
                                .compose(historyTransformer())
                                .map { HISTORY to it }
                        Flowable.mergeArray(
                                deviceSuggestions,
                                googleSuggestions,
                                historySuggestions
                        )
                    }
        }
    }

    /**
     * Fetches suggestions from Google and converts it to {@link GoogleSuggestionItem}
     */
    private fun googleTransformer(): FlowableTransformer<String, List<SuggestionItem>> {
        return FlowableTransformer { query ->
            if (!Utils.isOnline(application)) {
                Flowable.just(emptyList())
            } else query
                    .compose(RxJavaInterop.toV2Transformer(RxSuggestions.suggestionsTransformer(suggestionsLimit)))
                    .map<List<SuggestionItem>> {
                        it.map { query -> GoogleSuggestionItem(query) }
                    }.onErrorReturn { emptyList() }
        }
    }

    /**
     * Fetches matching items from History database and converts them to list of suggestions.
     */
    private fun historyTransformer(): FlowableTransformer<String, List<SuggestionItem>> {
        return FlowableTransformer { query ->
            query.debounce(suggestionsDebounce, TimeUnit.MILLISECONDS)
                    .switchMap { RxJavaInterop.toV2Flowable(historyRepository.search(it)) }
                    .map<List<SuggestionItem>> { suggestions ->
                        suggestions.asSequence()
                                .map { website ->
                                    HistorySuggestionItem(
                                            website,
                                            website.safeLabel(),
                                            website.url
                                    )
                                }.toList()
                    }.onErrorReturn { emptyList() }
        }
    }
}
