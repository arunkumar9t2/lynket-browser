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

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Pair;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import arun.com.chromer.data.website.model.WebColor;
import arun.com.chromer.data.website.model.Website;
import rx.Observable;

/**
 * Created by arunk on 24-02-2017.
 */
public interface WebsiteStore {
  @NonNull
  Observable<Website> getWebsite(@NonNull String url);

  @NonNull
  Observable<Void> clearCache();

  @NonNull
  Observable<Website> saveWebsite(@NonNull Website website);

  @NonNull
  Observable<WebColor> getWebsiteColor(@NonNull final String url);

  Observable<WebColor> saveWebsiteColor(@NonNull final String host, @ColorInt int color);

  @NonNull
  Pair<Bitmap, Integer> getWebsiteIconAndColor(@NonNull Website website);

  @NonNull
  Pair<Drawable, Integer> getWebsiteRoundIconAndColor(Website website);

  @NonNull
  Pair<Bitmap, Integer> getWebsiteIconWithPlaceholderAndColor(Website website);
}
