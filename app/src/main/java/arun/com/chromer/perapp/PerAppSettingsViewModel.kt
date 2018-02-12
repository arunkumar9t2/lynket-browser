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

class PerAppSettingsViewModel
@Inject
constructor(private val appRepository: AppRepository) : ViewModel() {
    private val subs = CompositeSubscription()

    private val loadingQueue = PublishSubject.create<Int>()
    private val blacklistQueue = PublishSubject.create<Pair<String, Boolean>>()
    private val incognitoQueue = PublishSubject.create<Pair<String, Boolean>>()

    val loadingLiveData = MutableLiveData<Boolean>()
    val appsLiveData = MutableLiveData<List<App>>()
    val appLiveData = MutableLiveData<Pair<Int, App>>()

    init {
        initAppsLoader()
        initIncognitoSub()
        initBlacklistSub()
    }

    private fun initAppsLoader() {
        subs.add(loadingQueue.asObservable()
                .doOnNext { loading(true) }
                .concatMap { appRepository.allApps().compose(SchedulerProvider.applyIoSchedulers()) }
                .doOnNext { loading(false) }
                .subscribe({ apps ->
                    Timber.d("Apps loaded ${apps.size}")
                    appsLiveData.value = apps
                }, Timber::e))
    }

    private fun initIncognitoSub() {
        subs.add(incognitoQueue.asObservable()
                .filter { !loadingLiveData.value!! }
                .doOnNext { loading(true) }
                .concatMap { (packageName, incognito) ->
                    if (incognito) {
                        appRepository.setPackageIncognito(packageName)
                    } else {
                        appRepository.removeIncognito(packageName)
                    }
                }.doOnNext { loading(false) }
                .map { app -> Pair(appsLiveData.value!!.indexOfFirst { it.packageName == app.packageName }, app) }
                .compose(SchedulerProvider.applyIoSchedulers())
                .subscribe { appLiveData.value = it })
    }

    private fun initBlacklistSub() {
        subs.add(blacklistQueue.asObservable()
                .filter { !loadingLiveData.value!! }
                .doOnNext { loading(true) }
                .concatMap { (packageName, blacklisted) ->
                    if (blacklisted) {
                        appRepository.setPackageBlacklisted(packageName)
                    } else {
                        appRepository.removeBlacklist(packageName)
                    }
                }.doOnNext { loading(false) }
                .map { app -> Pair(appsLiveData.value!!.indexOfFirst { it.packageName == app.packageName }, app) }
                .compose(SchedulerProvider.applyIoSchedulers())
                .subscribe { appLiveData.value = it })
    }

    fun incognito(selections: Pair<String, Boolean>) {
        incognitoQueue.onNext(selections)
    }

    fun blacklist(selections: Pair<String, Boolean>) {
        blacklistQueue.onNext(selections)
    }

    fun loadApps() {
        loadingQueue.onNext(0)
    }

    private fun loading(loading: Boolean) {
        loadingLiveData.postValue(loading)
    }

    override fun onCleared() {
        subs.clear()
    }
}

