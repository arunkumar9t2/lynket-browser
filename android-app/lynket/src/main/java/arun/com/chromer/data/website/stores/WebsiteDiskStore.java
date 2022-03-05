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

package arun.com.chromer.data.website.stores;

import static arun.com.chromer.shared.Constants.NO_COLOR;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Pair;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.common.BookStore;
import arun.com.chromer.data.website.model.WebColor;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.shared.Constants;
import in.arunkumarsampath.diskcache.ParcelDiskCache;
import io.paperdb.Book;
import io.paperdb.Paper;
import rx.Observable;
import timber.log.Timber;

/**
 * Cache store to get/put {@link Website} objects to disk cache.
 */
@Singleton
public class WebsiteDiskStore implements WebsiteStore, BookStore {
  public static final Pair<Bitmap, Integer> EMPTY_ICON_COLOR_PAIR = new Pair<>(null, Constants.NO_COLOR);
  public static final Pair<Drawable, Integer> EMPTY_DRAWABLE_PAIR = new Pair<>(null, Constants.NO_COLOR);
  // Cache size, currently set at 30 MB.
  private static final int DISK_CACHE_SIZE = 1024 * 1024 * 30;
  private static final String THEME_COLOR_BOOK = "THEME_COLOR_BOOK";
  // Cache to store our data.
  private ParcelDiskCache<Website> webSiteDiskCache;

  @Inject
  WebsiteDiskStore(Application context) {
    try {
      webSiteDiskCache = ParcelDiskCache.open(context, Website.class.getClassLoader(), "WebSiteCache", DISK_CACHE_SIZE);
    } catch (IOException e) {
      Timber.e(e);
    }
  }

  @Override
  public Book getBook() {
    return Paper.book(THEME_COLOR_BOOK);
  }

  @NonNull
  @Override
  public Observable<Website> getWebsite(@NonNull final String url) {
    return Observable.fromCallable(() -> {
      try {
        return webSiteDiskCache.get(url.trim());
      } catch (Exception e) {
        Timber.e(e);
        return null;
      }
    }).doOnNext(webSite -> {
      if (webSite == null) {
        Timber.d("Cache miss for: %s", url);
      } else {
        Timber.d("Cache hit for : %s", url);
      }
    });
  }

  @NonNull
  @Override
  public Observable<Void> clearCache() {
    return Observable.fromCallable(() -> {
      if (webSiteDiskCache != null) {
        webSiteDiskCache.clear();
      }
      return null;
    });
  }

  @NonNull
  @Override
  public Observable<Website> saveWebsite(@NonNull final Website website) {
    return Observable.fromCallable(() -> {
      try {
        return webSiteDiskCache.set(website.url, website);
      } catch (Exception e) {
        Timber.e(e);
        return null;
      }
    }).doOnNext(webSite1 -> {
      if (webSite1 != null) {
        Timber.d("Put %s to cache", webSite1.url);
      }
    });
  }

  @NonNull
  @Override
  public Observable<WebColor> getWebsiteColor(@NonNull final String url) {
    return Observable.fromCallable(() -> {
      try {
        return (WebColor) getBook().read(Uri.parse(url).getHost());
      } catch (Exception e) {
        Timber.e(e);
        return null;
      }
    }).map(webColor -> {
      if (webColor == null) {
        return new WebColor(Uri.parse(url).getHost(), NO_COLOR);
      } else return webColor;
    });
  }

  @Override
  public Observable<WebColor> saveWebsiteColor(@NonNull String host, @ColorInt int color) {
    return Observable.fromCallable(() -> {
      try {
        return getBook().write(host, new WebColor(host, color)).read(host);
      } catch (Exception e) {
        Timber.e(e);
        return new WebColor(host, NO_COLOR);
      }
    });
  }

  @NonNull
  @Override
  public Pair<Bitmap, Integer> getWebsiteIconAndColor(@NonNull Website website) {
    return EMPTY_ICON_COLOR_PAIR;
  }

  @NonNull
  @Override
  public Pair<Drawable, Integer> getWebsiteRoundIconAndColor(Website website) {
    return EMPTY_DRAWABLE_PAIR;
  }

  @NonNull
  @Override
  public Pair<Bitmap, Integer> getWebsiteIconWithPlaceholderAndColor(Website website) {
    return EMPTY_ICON_COLOR_PAIR;
  }
}
