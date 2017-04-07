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

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.List;

import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.data.website.model.WebSite;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by arunk on 03-03-2017.
 */
public class HistoryRepository implements HistoryStore {
    private static HistoryRepository INSTANCE = null;
    private final Context context;

    private HistoryRepository(Context applicationContext) {
        this.context = applicationContext;
    }

    public static HistoryRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new HistoryRepository(context.getApplicationContext());
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public Observable<Cursor> getAllItemsCursor() {
        return HistoryDiskStore.getInstance(context).getAllItemsCursor();
    }

    @NonNull
    @Override
    public Observable<WebSite> get(@NonNull WebSite webSite) {
        return HistoryDiskStore.getInstance(context).get(webSite)
                .doOnNext(saved -> {
                    if (saved == null) {
                        Timber.d("History miss for: %s", webSite.url);
                    } else {
                        Timber.d("History hit for : %s", webSite.url);
                    }
                });
    }

    @NonNull
    @Override
    public Observable<WebSite> insert(@NonNull final WebSite webSite) {
        if (Preferences.get(context).incognitoMode()) {
            return Observable.just(webSite);
        } else {
            return HistoryDiskStore.getInstance(context).insert(webSite)
                    .doOnNext(webSite1 -> {
                        if (webSite1 != null) {
                            Timber.d("Added %s to history", webSite1.url);
                        } else {
                            Timber.e("%s Did not add to history", webSite.url);
                        }
                    });
        }
    }

    @NonNull
    @Override
    public Observable<WebSite> update(@NonNull final WebSite webSite) {
        if (Preferences.get(context).incognitoMode()) {
            return Observable.just(webSite);
        } else {
            return HistoryDiskStore.getInstance(context).update(webSite)
                    .doOnNext(saved -> {
                        if (saved != null) {
                            Timber.d("Updated %s in history table", saved.url);
                        }
                    });
        }
    }

    @NonNull
    @Override
    public Observable<WebSite> delete(@NonNull WebSite webSite) {
        return HistoryDiskStore.getInstance(context).delete(webSite);
    }

    @NonNull
    @Override
    public Observable<Boolean> exists(@NonNull WebSite webSite) {
        return HistoryDiskStore.getInstance(context).exists(webSite);
    }

    @NonNull
    @Override
    public Observable<Integer> deleteAll() {
        return HistoryDiskStore.getInstance(context).deleteAll();
    }

    @NonNull
    @Override
    public Observable<List<WebSite>> recents() {
        return HistoryDiskStore.getInstance(context).recents();
    }
}
