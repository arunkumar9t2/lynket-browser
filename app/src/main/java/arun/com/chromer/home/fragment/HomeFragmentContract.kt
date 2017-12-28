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

package arun.com.chromer.home.fragment

import arun.com.chromer.data.history.HistoryRepository
import arun.com.chromer.data.website.model.Website
import arun.com.chromer.di.scopes.PerFragment
import arun.com.chromer.shared.base.Base
import arun.com.chromer.shared.base.Snackable
import arun.com.chromer.util.RxUtils
import timber.log.Timber
import javax.inject.Inject

interface HomeFragmentContract {
    interface View : Snackable, Base.View {
        fun setRecents(websites: List<Website>)
    }

    @PerFragment
    class Presenter @Inject
    constructor(
            private val historyRepository: HistoryRepository
    ) : Base.Presenter<View>() {

        fun loadRecents() {
            subs.add(historyRepository.recents()
                    .compose(RxUtils.applySchedulers())
                    .doOnError(Timber::e)
                    .doOnNext { webSites ->
                        if (isViewAttached) {
                            view.setRecents(webSites)
                        }
                    }.subscribe())
        }
    }
}
