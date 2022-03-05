/*
 *
 *  Lynket
 *
 *  Copyright (C) 2022 Arunkumar
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.perapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arun.com.chromer.data.apps.AppRepository
import arun.com.chromer.data.common.App
import arun.com.chromer.util.SchedulerProvider
import rx.android.schedulers.AndroidSchedulers
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
      .onBackpressureLatest()
      .doOnNext { loading(true) }
      .concatMap { appRepository.allApps().compose(SchedulerProvider.applyIoSchedulers()) }
      .doOnNext { loading(false) }
      .subscribe({ apps ->
        Timber.d("Apps loaded ${apps.size}")
        appsLiveData.value = apps
      }, Timber::e)
    )
  }

  private fun initIncognitoSub() {
    subs.add(incognitoQueue.asObservable()
      .onBackpressureLatest()
      .filter { !loadingLiveData.value!! }
      .doOnNext { loading(true) }
      .concatMap { (packageName, incognito) ->
        if (incognito) {
          appRepository.setPackageIncognito(packageName)
            .compose(SchedulerProvider.applyIoSchedulers())
        } else {
          appRepository.removeIncognito(packageName)
            .compose(SchedulerProvider.applyIoSchedulers())
        }
      }.observeOn(AndroidSchedulers.mainThread())
      .doOnNext { loading(false) }
      .map { app ->
        Pair(
          appsLiveData.value!!.indexOfFirst { it.packageName == app.packageName },
          app
        )
      }
      .subscribe { appLiveData.value = it })
  }

  private fun initBlacklistSub() {
    subs.add(blacklistQueue.asObservable()
      .onBackpressureLatest()
      .filter { !loadingLiveData.value!! }
      .doOnNext { loading(true) }
      .concatMap { (packageName, blacklisted) ->
        if (blacklisted) {
          appRepository.setPackageBlacklisted(packageName)
            .compose(SchedulerProvider.applyIoSchedulers())
        } else {
          appRepository.removeBlacklist(packageName)
            .compose(SchedulerProvider.applyIoSchedulers())
        }
      }.observeOn(AndroidSchedulers.mainThread())
      .doOnNext { loading(false) }
      .map { app ->
        Pair(
          appsLiveData.value!!.indexOfFirst { it.packageName == app.packageName },
          app
        )
      }
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

