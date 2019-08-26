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
    private val suggestionsSubject = BehaviorRelay.create<List<SuggestionItem>>()
    val suggestions: Observable<List<SuggestionItem>> = suggestionsSubject.hide()

    fun registerSearch(queryObservable: Observable<String>) {
        queryObservable
                .toFlowable(BackpressureStrategy.LATEST)
                .compose(RxJavaInterop.toV2Transformer(suggestionsEngine.suggestionsTransformer()))
                .toObservable()
                .doOnError(Timber::e)
                .takeUntil(detaches)
                .observeOn(schedulerProvider.ui)
                .subscribe { suggestionsSubject.accept(it) }
    }

    fun cleanUp() {
    }
}