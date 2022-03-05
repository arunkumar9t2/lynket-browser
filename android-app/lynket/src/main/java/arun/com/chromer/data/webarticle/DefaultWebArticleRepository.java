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

package arun.com.chromer.data.webarticle;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.common.qualifiers.Disk;
import arun.com.chromer.data.common.qualifiers.Network;
import arun.com.chromer.data.webarticle.model.WebArticle;
import rx.Observable;
import timber.log.Timber;

/**
 * Website repository implementation for managing and providing website data.
 */
@Singleton
public class DefaultWebArticleRepository implements WebArticleRepository {
  // Network store
  private final WebArticleStore articleNetworkStore;
  // Cache store
  private final WebArticleStore articleCacheStore;

  @Inject
  DefaultWebArticleRepository(@Network WebArticleStore articleNetworkStore, @Disk WebArticleStore articleCacheStore) {
    this.articleNetworkStore = articleNetworkStore;
    this.articleCacheStore = articleCacheStore;
  }

  @NonNull
  @Override
  public Observable<WebArticle> getWebArticle(@NonNull final String url) {
    return articleCacheStore.getWebArticle(url)
      .flatMap(webArticle -> {
        if (webArticle == null) {
          Timber.d("Cache miss for %s", url);
          return articleNetworkStore.getWebArticle(url)
            .flatMap(networkWebArticle -> {
              if (networkWebArticle != null) {
                return articleCacheStore.saveWebArticle(networkWebArticle);
              } else {
                return Observable.just(null);
              }
            });
        } else {
          Timber.d("Cache hit for %s", url);
          return Observable.just(webArticle);
        }
      });
  }
}
