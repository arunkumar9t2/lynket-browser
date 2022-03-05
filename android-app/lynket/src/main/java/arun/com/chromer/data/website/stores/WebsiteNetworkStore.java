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

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.website.model.WebColor;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.ColorUtil;
import arun.com.chromer.util.SchedulerProvider;
import arun.com.chromer.util.Utils;
import arun.com.chromer.util.glide.GlideApp;
import arun.com.chromer.util.parser.RxParser;
import rx.Observable;
import timber.log.Timber;

/**
 * Network store which freshly parses website data for a given URL.
 */
@Singleton
public class WebsiteNetworkStore implements WebsiteStore {
  private final Context context;

  @Inject
  WebsiteNetworkStore(@NonNull Application application) {
    this.context = application.getApplicationContext();
  }

  @NonNull
  @Override
  public Observable<Website> getWebsite(@NonNull String url) {
    return RxParser.INSTANCE.parseUrl(url)
      .flatMap(urlArticlePair -> {
        if (urlArticlePair.second != null) {
          final Website extractedWebsite = Website.fromArticle(urlArticlePair.second);
          // We preserve the original url, otherwise breaks cache.
          extractedWebsite.url = urlArticlePair.first;
          return Observable.just(extractedWebsite);
        } else {
          return Observable.just(new Website(urlArticlePair.first));
        }
      }).compose(SchedulerProvider.applyIoSchedulers());
  }

  @NonNull
  @Override
  public Observable<Void> clearCache() {
    return Observable.empty();
  }

  @NonNull
  @Override
  public Observable<Website> saveWebsite(@NonNull Website website) {
    return Observable.empty();
  }

  @NonNull
  @Override
  public Observable<WebColor> getWebsiteColor(@NonNull String url) {
    return Observable.empty();
  }

  @Override
  public Observable<WebColor> saveWebsiteColor(@NonNull String host, @ColorInt int color) {
    return Observable.empty();
  }

  @NonNull
  @Override
  public Pair<Bitmap, Integer> getWebsiteIconAndColor(@NonNull Website website) {
    if (TextUtils.isEmpty(website.faviconUrl)) {
      return new Pair<>(null, Constants.NO_COLOR);
    }
    int color = Constants.NO_COLOR;
    Bitmap icon = null;
    try {
      icon = GlideApp.with(context).asBitmap().load(website.faviconUrl).submit().get();
      final Palette palette = Palette.from(icon).generate();
      color = ColorUtil.getBestColorFromPalette(palette);
    } catch (Exception e) {
      Timber.e(e);
    }
    return new Pair<>(icon, color);
  }

  @NonNull
  @Override
  public Pair<Drawable, Integer> getWebsiteRoundIconAndColor(@NonNull Website website) {
    if (TextUtils.isEmpty(website.faviconUrl)) {
      return new Pair<>(null, Constants.NO_COLOR);
    }
    int color = Constants.NO_COLOR;
    Bitmap icon = null;
    try {
      icon = GlideApp.with(context).asBitmap().circleCrop().load(website.faviconUrl).submit().get();
      final Palette palette = Palette.from(icon).clearFilters().generate();
      color = ColorUtil.getBestColorFromPalette(palette);
    } catch (Exception e) {
      Timber.e(e);
    }
    if (Utils.isValidFavicon(icon)) {
      return new Pair<>(new BitmapDrawable(context.getResources(), icon), color);
    } else {
      return new Pair<>(null, color);
    }
  }

  @NonNull
  @Override
  public Pair<Bitmap, Integer> getWebsiteIconWithPlaceholderAndColor(@NonNull Website website) {
    int color = Constants.NO_COLOR;
    Bitmap icon = null;
    try {
      icon = GlideApp.with(context).asBitmap().load(website).submit().get();
      final Palette palette = Palette.from(icon).generate();
      color = ColorUtil.getBestColorFromPalette(palette);
    } catch (Exception e) {
      Timber.e(e);
    }
    return new Pair<>(icon, color);
  }
}
