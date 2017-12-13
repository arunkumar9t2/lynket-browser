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
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.history.model.HistoryTable;
import arun.com.chromer.data.website.model.Website;
import rx.Observable;
import timber.log.Timber;

import static arun.com.chromer.data.history.model.HistoryTable.ALL_COLUMN_PROJECTION;
import static arun.com.chromer.data.history.model.HistoryTable.COLUMN_AMP;
import static arun.com.chromer.data.history.model.HistoryTable.COLUMN_BOOKMARKED;
import static arun.com.chromer.data.history.model.HistoryTable.COLUMN_CANONICAL;
import static arun.com.chromer.data.history.model.HistoryTable.COLUMN_COLOR;
import static arun.com.chromer.data.history.model.HistoryTable.COLUMN_CREATED_AT;
import static arun.com.chromer.data.history.model.HistoryTable.COLUMN_FAVICON;
import static arun.com.chromer.data.history.model.HistoryTable.COLUMN_TITLE;
import static arun.com.chromer.data.history.model.HistoryTable.COLUMN_URL;
import static arun.com.chromer.data.history.model.HistoryTable.COLUMN_VISITED;
import static arun.com.chromer.data.history.model.HistoryTable.DATABASE_CREATE;
import static arun.com.chromer.data.history.model.HistoryTable.ORDER_BY_TIME_DESC;
import static arun.com.chromer.data.history.model.HistoryTable.TABLE_NAME;

/**
 * Created by Arunkumar on 03-03-2017.
 */
@Singleton
public class HistorySqlDiskStore extends SQLiteOpenHelper implements HistoryStore {
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase database;

    @Inject
    HistorySqlDiskStore(Application application) {
        super(application, TABLE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        Timber.d("onCreate called");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private synchronized boolean isOpen() {
        return database != null && database.isOpen();
    }

    public synchronized void open() {
        if (!isOpen()) {
            database = getWritableDatabase();
        }
    }

    public synchronized void close() {
        if (isOpen()) {
            database.close();
        }
    }

    @NonNull
    @Override
    public Observable<Cursor> getAllItemsCursor() {
        return Observable.fromCallable(() -> {
            open();
            return database.query(TABLE_NAME,
                    ALL_COLUMN_PROJECTION,
                    null,
                    null,
                    null,
                    null,
                    ORDER_BY_TIME_DESC);
        });
    }

    @NonNull
    @Override
    public Observable<Website> get(@NonNull final Website website) {
        return Observable.fromCallable(() -> {
            open();
            final Cursor cursor = database.rawQuery("SELECT * FROM " + HistoryTable.TABLE_NAME + " WHERE " + HistoryTable.COLUMN_URL + "=?", new String[]{website.url});
            if (cursor == null) {
                return null;
            } else if (cursor.getCount() == 0) {
                cursor.close();
                return null;
            } else {
                cursor.moveToFirst();
                final Website savedSite = Website.fromCursor(cursor);
                cursor.close();
                return savedSite;
            }
        });
    }

    @NonNull
    @Override
    public Observable<Website> insert(@NonNull Website website) {
        return exists(website)
                .flatMap(exists -> {
                    if (exists) {
                        return update(website);
                    } else {
                        final ContentValues values = new ContentValues();
                        values.put(COLUMN_URL, website.url);
                        values.put(COLUMN_TITLE, website.title);
                        values.put(COLUMN_FAVICON, website.faviconUrl);
                        values.put(COLUMN_CANONICAL, website.canonicalUrl);
                        values.put(COLUMN_COLOR, website.themeColor);
                        values.put(COLUMN_AMP, website.ampUrl);
                        values.put(COLUMN_BOOKMARKED, website.bookmarked);
                        values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
                        values.put(COLUMN_VISITED, 1);
                        if (database.insert(TABLE_NAME, null, values) != -1) {
                            return Observable.just(website);
                        } else {
                            return Observable.just(null);
                        }
                    }
                });
    }

    @NonNull
    @Override
    public Observable<Website> update(@NonNull Website website) {
        return get(website).flatMap(saved -> {
            if (saved != null) {
                final ContentValues values = new ContentValues();
                values.put(COLUMN_URL, saved.url);
                values.put(COLUMN_TITLE, saved.title);
                values.put(COLUMN_FAVICON, saved.faviconUrl);
                values.put(COLUMN_CANONICAL, saved.canonicalUrl);
                values.put(COLUMN_COLOR, saved.themeColor);
                values.put(COLUMN_AMP, saved.ampUrl);
                values.put(COLUMN_BOOKMARKED, saved.bookmarked);
                values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
                values.put(COLUMN_VISITED, ++saved.count);

                final String whereClause = COLUMN_URL + "=?";
                final String[] whereArgs = {saved.url};

                if (database.update(TABLE_NAME, values, whereClause, whereArgs) > 0) {
                    return Observable.just(saved);
                } else {
                    return Observable.just(website);
                }
            } else {
                return Observable.just(website);
            }
        });
    }

    @NonNull
    @Override
    public Observable<Website> delete(@NonNull Website website) {
        return Observable.fromCallable(() -> {
            open();
            final String whereClause = COLUMN_URL + "=?";
            final String[] whereArgs = {website.url};
            if (database.delete(TABLE_NAME, whereClause, whereArgs) > 0) {
                Timber.d("Deletion successful for %s", website.url);
            } else {
                Timber.e("Deletion failed for %s", website.url);
            }
            return website;
        });
    }

    @NonNull
    @Override
    public Observable<Boolean> exists(@NonNull Website website) {
        return Observable.fromCallable(() -> {
            open();
            final String selection = " " + COLUMN_URL + "=?";
            final String[] selectionArgs = {website.url};
            final Cursor cursor = database.query(TABLE_NAME,
                    ALL_COLUMN_PROJECTION,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            boolean exists = false;
            if (cursor != null && cursor.getCount() > 0) {
                exists = true;
            }
            if (cursor != null) {
                cursor.close();
            }
            return exists;
        });
    }

    @NonNull
    @Override
    public Observable<Integer> deleteAll() {
        return Observable.fromCallable(() -> {
            open();
            return database.delete(TABLE_NAME, "1", null);
        });
    }

    @NonNull
    @Override
    public Observable<List<Website>> recents() {
        return Observable.fromCallable(() -> {
            open();
            final List<Website> websites = new ArrayList<>();
            final Cursor cursor = database.query(TABLE_NAME,
                    ALL_COLUMN_PROJECTION,
                    null,
                    null,
                    null,
                    null,
                    ORDER_BY_TIME_DESC,
                    "8");
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        websites.add(Website.fromCursor(cursor));
                    }
                } finally {
                    cursor.close();
                }
            }
            return websites;
        });
    }

    @NonNull
    @Override
    public Observable<List<Website>> search(@NonNull String text) {
        return Observable.fromCallable(() -> {
            open();
            final List<Website> websites = new ArrayList<>();
            final Cursor cursor = database.query(true,
                    TABLE_NAME, ALL_COLUMN_PROJECTION,
                    "(" + COLUMN_URL + " like '%" + text + "%' OR " + COLUMN_TITLE + " like '%" + text + "%')", null,
                    null, null, ORDER_BY_TIME_DESC, "5");
            if (cursor != null) {
                try {
                    while (cursor.moveToNext()) {
                        websites.add(Website.fromCursor(cursor));
                    }
                } finally {
                    cursor.close();
                }
            }
            return websites;
        });
    }
}
