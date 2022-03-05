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

package arun.com.chromer.util

import rx.Observable
import rx.subjects.PublishSubject

/**
 * A simple event bus built with RxJava
 */
class RxEventBus {

  private val publishSubject: PublishSubject<Any> = PublishSubject.create()

  /**
   * Posts an object (usually an Event) to the bus
   */
  fun post(event: Any) {
    publishSubject.onNext(event)
  }

  /**
   * Observable that will emmit everything posted to the event bus.
   */
  fun events(): Observable<Any> = publishSubject.asObservable()

  /**
   * Observable that only emits events of a specific class.
   * Use this if you only want to subscribe to one type of events.
   */
  inline fun <reified T> filteredEvents(): Observable<T> = events().ofType(T::class.java)
}
