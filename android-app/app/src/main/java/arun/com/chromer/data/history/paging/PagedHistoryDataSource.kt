package arun.com.chromer.data.history.paging

import android.arch.paging.DataSource
import android.arch.paging.PositionalDataSource
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
    ) = callback.onResult(historyStore.loadHistoryRange(
            params.requestedLoadSize,
            params.requestedStartPosition
    ), 0)


    @Singleton
    class Factory
    @Inject
    constructor(
            private val pagedHistoryDataSourceProvider: Provider<PagedHistoryDataSource>
    ) : DataSource.Factory<Int, Website>() {

        override fun create(): PagedHistoryDataSource = pagedHistoryDataSourceProvider.get()
    }
}
