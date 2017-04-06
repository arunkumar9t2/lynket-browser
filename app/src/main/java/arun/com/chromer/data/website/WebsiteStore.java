/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.data.website;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import arun.com.chromer.data.website.model.WebColor;
import arun.com.chromer.data.website.model.WebSite;
import rx.Observable;

/**
 * Created by arunk on 24-02-2017.
 */

interface WebsiteStore {
    @NonNull
    Observable<WebSite> getWebsite(@NonNull String url);

    @NonNull
    Observable<Void> clearCache();

    @NonNull
    Observable<WebSite> saveWebsite(@NonNull WebSite webSite);

    @NonNull
    Observable<WebColor> getWebsiteColor(@NonNull final String url);

    Observable<WebColor> saveWebsiteColor(@NonNull final String host, @ColorInt int color);
}
