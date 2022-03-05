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

package arun.com.chromer.util.epoxy

import com.airbnb.epoxy.DiffResult
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.OnModelBuildFinishedListener
import io.reactivex.Observable

fun EpoxyController.buildEvents(): Observable<DiffResult> = Observable.create { emitter ->
  val buildListener = OnModelBuildFinishedListener { result: DiffResult ->
    emitter.onNext(result)
  }.also(::addModelBuildListener)
  emitter.setCancellable { removeModelBuildListener(buildListener) }
}

fun EpoxyController.intercepts(): Observable<List<EpoxyModel<*>>> = Observable.create { emitter ->
  val interceptor = EpoxyController.Interceptor { models ->
    emitter.onNext(models)
  }.also(::addInterceptor)
  emitter.setCancellable { removeInterceptor(interceptor) }
}
