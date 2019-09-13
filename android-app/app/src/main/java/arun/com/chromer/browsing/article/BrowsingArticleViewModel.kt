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

package arun.com.chromer.browsing.article

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arun.com.chromer.data.Result
import arun.com.chromer.data.webarticle.WebArticleRepository
import arun.com.chromer.data.webarticle.model.WebArticle
import arun.com.chromer.search.provider.SearchProvider
import arun.com.chromer.search.provider.SearchProviders
import arun.com.chromer.settings.RxPreferences
import arun.com.chromer.util.SchedulerProvider
import io.reactivex.Observable
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * A simple view model delivering a {@link Website} from repo.
 */
class BrowsingArticleViewModel
@Inject
constructor(
        private val webArticleRepository: WebArticleRepository,
        private val schedulerProvider: dev.arunkumar.android.rxschedulers.SchedulerProvider,
        private val searchProviders: SearchProviders,
        private val rxPreferences: RxPreferences
) : ViewModel() {
    private val subs = CompositeSubscription()

    private val loadingQueue = PublishSubject.create<String>()

    val articleLiveData = MutableLiveData<Result<WebArticle>>()

    init {
        subs.add(loadingQueue.asObservable()
                .concatMap {
                    webArticleRepository
                            .getWebArticle(it)
                            .compose(SchedulerProvider.applyIoSchedulers())
                            .compose(Result.applyToObservable())
                }.subscribe { articleLiveData.value = it })
    }

    val selectedSearchProvider: Observable<SearchProvider> = rxPreferences
            .searchEngine
            .observe()
            .observeOn(schedulerProvider.pool)
            .switchMap { selectedEngine ->
                searchProviders
                        .availableProviders
                        .toObservable()
                        .flatMapIterable { it }
                        .filter { it.name == selectedEngine }
                        .first(SearchProviders.GOOGLE_SEARCH_PROVIDER)
                        .toObservable()
            }.replay(1)
            .refCount()
            .observeOn(schedulerProvider.ui)

    fun loadArticle(url: String) = loadingQueue.onNext(url)

    override fun onCleared() {
        subs.clear()
    }
}
