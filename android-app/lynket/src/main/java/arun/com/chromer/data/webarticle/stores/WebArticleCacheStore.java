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

package arun.com.chromer.data.webarticle.stores;

import android.app.Application;

import androidx.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.webarticle.WebArticleStore;
import arun.com.chromer.data.webarticle.model.WebArticle;
import in.arunkumarsampath.diskcache.ParcelDiskCache;
import rx.Observable;
import timber.log.Timber;

/**
 * Cache store to get/put {@link WebArticle} objects to disk cache.
 */
@Singleton
public class WebArticleCacheStore implements WebArticleStore {
  private static final String TAG = WebArticleCacheStore.class.getSimpleName();
  // Cache size, currently set at 30 MB.
  private static final int DISK_CACHE_SIZE = 1024 * 1024 * 30;
  // Disk LRU cache to store articles
  private ParcelDiskCache<WebArticle> webSiteDiskCache;

  @Inject
  WebArticleCacheStore(Application application) {
    try {
      webSiteDiskCache = ParcelDiskCache.open(application, WebArticle.class.getClassLoader(), WebArticle.class.getName(), DISK_CACHE_SIZE);
    } catch (IOException ignored) {
      Timber.e(ignored);
    }
  }

  @NonNull
  @Override
  public Observable<WebArticle> getWebArticle(@NonNull final String url) {
    return Observable.fromCallable(() -> {
      try {
        return webSiteDiskCache.get(url.trim());
      } catch (Exception e) {
        return null;
      }
    });
  }

  @NonNull
  @Override
  public Observable<WebArticle> saveWebArticle(@NonNull final WebArticle webSite) {
    return Observable.fromCallable(() -> {
      try {
        return webSiteDiskCache.set(webSite.url, webSite);
      } catch (Exception e) {
        return webSite;
      }
    });
  }
}
