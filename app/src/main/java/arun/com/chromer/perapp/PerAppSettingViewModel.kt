package arun.com.chromer.perapp

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import arun.com.chromer.data.apps.AppRepository
import arun.com.chromer.data.common.App
import arun.com.chromer.util.SchedulerProvider
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by arunk on 10-02-2018.
 */

class PerAppSettingViewModel
@Inject
constructor(private val appRepository: AppRepository) : ViewModel() {
    private val subs = CompositeSubscription()

    private val loadingQueue = PublishSubject.create<Int>()

    val loadingLiveData = MutableLiveData<Boolean>()
    val appsLiveData = MutableLiveData<List<App>>()

    init {
        initAppsLoader()
    }

    private fun initAppsLoader() {
        subs.add(loadingQueue.asObservable()
                .doOnNext { loadingLiveData.postValue(true) }
                .concatMap { appRepository.allApps() }
                .compose(SchedulerProvider.applyIoSchedulers())
                .doOnNext { loadingLiveData.postValue(false) }
                .subscribe({ apps ->
                    Timber.d("Apps loaded ${apps.size}")
                    appsLiveData.value = apps
                }, Timber::e))
    }

    fun loadApps() {
        loadingQueue.onNext(0)
    }

    override fun onCleared() {
        subs.clear()
    }
}