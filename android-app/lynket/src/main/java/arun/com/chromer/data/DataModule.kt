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

import arun.com.chromer.data.apps.AppRepository
import arun.com.chromer.data.apps.DefaultAppRepository
import arun.com.chromer.data.apps.qualifiers.System
import arun.com.chromer.data.apps.store.AppDiskStore
import arun.com.chromer.data.apps.store.AppStore
import arun.com.chromer.data.apps.store.AppSystemStore
import arun.com.chromer.data.common.qualifiers.Disk
import arun.com.chromer.data.common.qualifiers.Network
import arun.com.chromer.data.history.DefaultHistoryRepository
import arun.com.chromer.data.history.HistoryRepository
import arun.com.chromer.data.history.HistorySqlDiskStore
import arun.com.chromer.data.history.HistoryStore
import arun.com.chromer.data.webarticle.DefaultWebArticleRepository
import arun.com.chromer.data.webarticle.WebArticleRepository
import arun.com.chromer.data.webarticle.WebArticleStore
import arun.com.chromer.data.webarticle.stores.WebArticleCacheStore
import arun.com.chromer.data.webarticle.stores.WebArticleNetworkStore
import arun.com.chromer.data.website.DefaultWebsiteRepository
import arun.com.chromer.data.website.WebsiteRepository
import arun.com.chromer.data.website.stores.WebsiteDiskStore
import arun.com.chromer.data.website.stores.WebsiteNetworkStore
import arun.com.chromer.data.website.stores.WebsiteStore
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class DataModule {

  @Disk
  @Binds
  @Singleton
  internal abstract fun appDiskStore(appDiskStore: AppDiskStore): AppStore

  @System
  @Binds
  @Singleton
  internal abstract fun appSystemStore(appSystemStore: AppSystemStore): AppStore

  @Binds
  @Singleton
  internal abstract fun appRepository(appRepository: DefaultAppRepository): AppRepository

  @Binds
  @Singleton
  internal abstract fun historyStore(historySqlDiskStore: HistorySqlDiskStore): HistoryStore

  @Binds
  @Singleton
  internal abstract fun historyRepository(historyRepository: DefaultHistoryRepository): HistoryRepository

  @Binds
  @Singleton
  @Disk
  internal abstract fun websiteDiskStore(websiteDiskStore: WebsiteDiskStore): WebsiteStore

  @Binds
  @Singleton
  @Network
  internal abstract fun websiteNetworkStore(websiteNetworkStore: WebsiteNetworkStore): WebsiteStore

  @Binds
  @Singleton
  internal abstract fun websiteRepository(websiteRepository: DefaultWebsiteRepository): WebsiteRepository

  @Binds
  @Singleton
  @Disk
  internal abstract fun diskWebArticleStore(webArticleCacheStore: WebArticleCacheStore): WebArticleStore

  @Binds
  @Singleton
  @Network
  internal abstract fun networkWebArticleStore(websiteNetworkStore: WebArticleNetworkStore): WebArticleStore

  @Binds
  @Singleton
  internal abstract fun webArticleRepository(webArticleRepository: DefaultWebArticleRepository): WebArticleRepository
}
