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

package arun.com.chromer.shortcuts

import android.arch.lifecycle.ViewModel
import arun.com.chromer.data.Result
import arun.com.chromer.data.website.BaseWebsiteRepository
import arun.com.chromer.data.website.model.WebSite
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import javax.inject.Inject

class HomeScreenShortcutViewModel
@Inject
constructor(private val websiteRepository: BaseWebsiteRepository) : ViewModel() {

    private val webSiteSubject = BehaviorSubject.create<Result<WebSite>>(Result.Idle<WebSite>())

    fun loadWebSiteDetails(url: String): Observable<Result<WebSite>> {
        if (webSiteSubject.value is Result.Idle<WebSite>) {
            websiteRepository.getWebsite(url)
                    .map { Result.Success(it) as Result<WebSite> }
                    .onErrorReturn { Result.Failure(it) }
                    .startWith(Result.Loading())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(webSiteSubject)
        }
        return webSiteSubject
    }
}
