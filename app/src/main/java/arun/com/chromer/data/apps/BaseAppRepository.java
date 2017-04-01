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

package arun.com.chromer.data.apps;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import arun.com.chromer.data.common.App;
import rx.Observable;

interface BaseAppRepository {
    @NonNull
    Observable<App> getApp(@NonNull String packageName);

    @NonNull
    Observable<App> savApp(App app);

    boolean isPackageBlacklisted(@NonNull String packageName);

    Observable<App> setPackageBlacklisted(@NonNull String packageName);

    @ColorInt
    int getPackageColorSync(@NonNull String packageName);

    Observable<Integer> getPackageColor(@NonNull String packageName);

    Observable<App> setPackageColor(@NonNull String packageName, int color);

    Observable<App> removeBlacklist(String packageName);
}
