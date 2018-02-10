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

package arun.com.chromer.browsing

import android.annotation.TargetApi
import android.app.ActivityManager
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Build
import arun.com.chromer.data.Result
import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.settings.Preferences
import arun.com.chromer.shared.Constants
import arun.com.chromer.util.SchedulerProvider
import arun.com.chromer.util.Utils
import arun.com.chromer.util.compat.TaskDescriptionCompat
import rx.Observable
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject

/**
 * A simple view model delivering a {@link Website} from repo and handling related tasks.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class BrowsingViewModel
@Inject
constructor(
        private val preferences: Preferences,
        private val websiteRepository: WebsiteRepository
) : ViewModel() {
    private val subs = CompositeSubscription()

    var isIncognito: Boolean = false

    private val websiteQueue = PublishSubject.create<String>()
    private val taskDescriptionQueue = PublishSubject.create<Website>()

    val toolbarColor = MutableLiveData<Int>()
    val websiteLiveData = MutableLiveData<Result<Website>>()
    val activityDescription = MutableLiveData<ActivityManager.TaskDescription>()

    init {
        // Monitor website requests
        subs.add(websiteQueue.filter { it.isNotEmpty() }
                .switchMap { url -> websiteObservable(url).compose(Result.applyToObservable()) }
                .compose(SchedulerProvider.applyIoSchedulers())
                .doOnError(Timber::e)
                .subscribe({ result ->
                    websiteLiveData.value = result
                    if (result is Result.Success) {
                        taskDescriptionQueue.onNext(result.data!!)
                    }
                }))

        // Set task descriptions
        subs.add(taskDescriptionQueue
                .concatMap { website ->
                    return@concatMap Observable.just(website)
                            .map { TaskDescriptionCompat(website.safeLabel(), null, toolbarColor.value!!) }
                            .doOnNext(this::setTaskDescription)
                            .map {
                                val iconColor = websiteRepository.getWebsiteIconWithPlaceholderAndColor(website)
                                val selectedToolbarColor = when {
                                    !preferences.dynamiceToolbarEnabledAndWebEnabled() -> toolbarColor.value!!
                                    website.themeColor() != Constants.NO_COLOR -> website.themeColor()
                                    else -> iconColor.second
                                }
                                return@map TaskDescriptionCompat(website.safeLabel(), iconColor.first, selectedToolbarColor)
                            }.doOnNext(this::setTaskDescription)
                            .doOnError(Timber::e)
                            .compose(SchedulerProvider.applyIoSchedulers())
                }.subscribe())
    }

    private fun websiteObservable(url: String) = if (!isIncognito) {
        websiteRepository.getWebsite(url)
    } else websiteRepository.getIncognitoWebsite(url)

    private fun setTaskDescription(task: TaskDescriptionCompat?) {
        task?.let {
            toolbarColor.postValue(task.color)
            if (Utils.ANDROID_LOLLIPOP) {
                activityDescription.postValue(task.toActivityTaskDescription())
            }
        }
    }

    fun loadWebSiteDetails(url: String) {
        websiteQueue.onNext(url)
    }

    override fun onCleared() {
        subs.clear()
    }
}
