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

package arun.com.chromer.data.history.paging

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import arun.com.chromer.data.history.HistoryStore
import arun.com.chromer.data.website.model.Website
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class PagedHistoryDataSource
@Inject
constructor(private val historyStore: HistoryStore) : PositionalDataSource<Website>() {

  override fun loadRange(
    params: LoadRangeParams,
    callback: LoadRangeCallback<Website>
  ) = callback.onResult(historyStore.loadHistoryRange(params.loadSize, params.startPosition))

  override fun loadInitial(
    params: LoadInitialParams,
    callback: LoadInitialCallback<Website>
  ) = callback.onResult(
    historyStore.loadHistoryRange(
      params.requestedLoadSize,
      params.requestedStartPosition
    ), 0
  )


  @Singleton
  class Factory
  @Inject
  constructor(
    private val pagedHistoryDataSourceProvider: Provider<PagedHistoryDataSource>
  ) : DataSource.Factory<Int, Website>() {

    override fun create(): PagedHistoryDataSource = pagedHistoryDataSourceProvider.get()
  }
}
