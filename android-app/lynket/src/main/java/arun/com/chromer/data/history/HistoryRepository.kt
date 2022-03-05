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

package arun.com.chromer.data.history

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import arun.com.chromer.data.website.model.Website
import rx.Observable

/**
 * Created by Arunkumar on 03-03-2017.
 */
interface HistoryRepository {
  fun get(website: Website): Observable<Website>

  fun insert(website: Website): Observable<Website>

  fun update(website: Website): Observable<Website>

  fun delete(website: Website): Observable<Website>

  fun exists(website: Website): Observable<Boolean>

  fun deleteAll(): Observable<Int>

  fun recents(): io.reactivex.Observable<List<Website>>

  fun search(text: String): Observable<List<Website>>

  /**
   * Load given range specified by [limit] and [offset]
   */
  fun loadHistoryRange(limit: Int, offset: Int): List<Website>

  fun pagedHistory(): LiveData<PagedList<Website>>

  fun changes(): io.reactivex.Observable<Int>
}
