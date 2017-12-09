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

package arun.com.chromer.data.history;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.List;

import arun.com.chromer.data.website.model.WebSite;
import rx.Observable;

/**
 * Created by Arunkumar on 03-03-2017.
 */
public interface BaseHistoryRepository {
    @NonNull
    Observable<Cursor> getAllItemsCursor();

    @NonNull
    Observable<WebSite> get(@NonNull final WebSite webSite);

    @NonNull
    Observable<WebSite> insert(@NonNull final WebSite webSite);

    @NonNull
    Observable<WebSite> update(@NonNull final WebSite webSite);

    @NonNull
    Observable<WebSite> delete(@NonNull final WebSite webSite);

    @NonNull
    Observable<Boolean> exists(@NonNull final WebSite webSite);

    @NonNull
    Observable<Integer> deleteAll();

    @NonNull
    Observable<List<WebSite>> recents();

    @NonNull
    Observable<List<WebSite>> search(@NonNull String text);
}
