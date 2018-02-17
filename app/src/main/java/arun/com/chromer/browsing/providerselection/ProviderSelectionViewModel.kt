package arun.com.chromer.browsing.providerselection

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import arun.com.chromer.data.apps.AppRepository
import arun.com.chromer.data.apps.model.Provider
import arun.com.chromer.util.SchedulerProvider
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by arunk on 17-02-2018.
 */
class ProviderSelectionViewModel
@Inject
constructor(
        private val appRepository: AppRepository
) : ViewModel() {
    val subs = CompositeSubscription()

    private val loadingQueue = PublishSubject.create<Int>()

    val providersLiveData = MutableLiveData<List<Provider>>()

    init {
        subs.add(loadingQueue.asObservable()
                .switchMap {
                    return@switchMap appRepository
                            .allProviders()
                            .compose(SchedulerProvider.applyIoSchedulers())
                }.subscribe({ providers ->
                    providersLiveData.value = providers
                }, Timber::e))
    }

    fun loadProviders() {
        loadingQueue.onNext(0)
    }

    override fun onCleared() {
        subs.clear()
    }
}