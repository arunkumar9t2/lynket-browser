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

package arun.com.chromer.data

import rx.Observable

sealed class Result<T> {
  data class Success<T>(val data: T?) : Result<T>()
  class Loading<T> : Result<T>()
  class Idle<T> : Result<T>()
  data class Failure<T>(val throwable: Throwable) : Result<T>()


  companion object {
    fun <T> applyToObservable(): Observable.Transformer<T, Result<T>> {
      return Observable.Transformer { sourceObservable ->
        sourceObservable
          .map { Success(it) as Result<T> }
          .onErrorReturn { Failure(it) }
          .startWith(Loading())
      }
    }
  }
}
