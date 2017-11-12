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

import android.app.Application;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.data.website.model.WebSite;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by arunk on 03-03-2017.
 */
@Singleton
public class HistoryRepository implements BaseHistoryRepository {
    private final Application application;
    private final HistoryStore historyStore;
    private final Preferences preferences;

    @Inject
    HistoryRepository(Application application, HistoryStore historyStore, Preferences preferences) {
        this.application = application;
        this.historyStore = historyStore;
        this.preferences = preferences;
    }

    @NonNull
    @Override
    public Observable<Cursor> getAllItemsCursor() {
        return historyStore.getAllItemsCursor();
    }

    @NonNull
    @Override
    public Observable<WebSite> get(@NonNull WebSite webSite) {
        return historyStore.get(webSite)
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
        if (preferences.incognitoMode()) {
            return Observable.just(webSite);
        } else {
            return historyStore.insert(webSite)
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
        if (preferences.incognitoMode()) {
            return Observable.just(webSite);
        } else {
            return historyStore.update(webSite)
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
        return historyStore.delete(webSite);
    }

    @NonNull
    @Override
    public Observable<Boolean> exists(@NonNull WebSite webSite) {
        return historyStore.exists(webSite);
    }

    @NonNull
    @Override
    public Observable<Integer> deleteAll() {
        return historyStore.deleteAll();
    }

    @NonNull
    @Override
    public Observable<List<WebSite>> recents() {
        return historyStore.recents();
    }
}
