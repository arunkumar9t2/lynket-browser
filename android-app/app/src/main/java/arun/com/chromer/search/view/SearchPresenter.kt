package arun.com.chromer.search.view

import android.annotation.SuppressLint
import arun.com.chromer.di.scopes.PerView
import arun.com.chromer.di.view.Detaches
import arun.com.chromer.search.suggestion.SuggestionsEngine
import arun.com.chromer.search.suggestion.items.SuggestionItem
import arun.com.chromer.search.suggestion.items.SuggestionType
import com.jakewharton.rxrelay2.PublishRelay
import dev.arunkumar.android.rxschedulers.SchedulerProvider
import io.reactivex.BackpressureStrategy.LATEST
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject

@SuppressLint("CheckResult")
@PerView
class SearchPresenter
@Inject
constructor(
        private val suggestionsEngine: SuggestionsEngine,
        private val schedulerProvider: SchedulerProvider,
        @param:Detaches
        private val detaches: Observable<Unit>
) {
    private val suggestionsSubject = PublishRelay.create<Pair<SuggestionType, List<SuggestionItem>>>()
    val suggestions: Observable<Pair<SuggestionType, List<SuggestionItem>>> = suggestionsSubject.hide()

    fun registerSearch(queryObservable: Observable<String>) {
        queryObservable
                .toFlowable(LATEST)
                .debounce(200, MILLISECONDS, schedulerProvider.pool)
                .compose(suggestionsEngine.suggestionsTransformer())
                .doOnError(Timber::e)
                .takeUntil(detaches.toFlowable(LATEST))
                .subscribe(suggestionsSubject::accept)
    }
}