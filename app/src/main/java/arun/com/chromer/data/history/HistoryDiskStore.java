package arun.com.chromer.data.history;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import arun.com.chromer.data.history.model.HistoryTable;
import arun.com.chromer.data.website.model.WebSite;
import rx.Observable;
import rx.functions.Func1;
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
import static arun.com.chromer.data.history.model.HistoryTable.TABLE_NAME;

/**
 * Created by Arunkumar on 03-03-2017.
 */
public class HistoryDiskStore extends SQLiteOpenHelper implements HistoryStore {
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase database;

    private HistoryDiskStore(Context context) {
        super(context, TABLE_NAME, null, DATABASE_VERSION);
    }

    private static HistoryStore INSTANCE = null;

    public static HistoryStore getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new HistoryDiskStore(context.getApplicationContext());
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(HistoryTable.DATABASE_CREATE);
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
                    HistoryTable.ORDER_BY_TIME_DESC);
        });
    }

    @NonNull
    @Override
    public Observable<WebSite> get(@NonNull final WebSite webSite) {
        return Observable.fromCallable(() -> {
            open();
            final Cursor cursor = database.rawQuery("SELECT * FROM " + HistoryTable.TABLE_NAME + " WHERE " + HistoryTable.COLUMN_URL + "=?", new String[]{webSite.url});
            if (cursor == null) {
                return null;
            } else if (cursor.getCount() == 0) {
                cursor.close();
                return null;
            } else {
                cursor.moveToFirst();
                final WebSite savedSite = WebSite.fromCursor(cursor);
                cursor.close();
                return savedSite;
            }
        });
    }

    @NonNull
    @Override
    public Observable<WebSite> insert(@NonNull WebSite webSite) {
        return exists(webSite)
                .flatMap(new Func1<Boolean, Observable<WebSite>>() {
                    @Override
                    public Observable<WebSite> call(Boolean exists) {
                        if (exists) {
                            return update(webSite);
                        } else {
                            final ContentValues values = new ContentValues();
                            values.put(COLUMN_URL, webSite.url);
                            values.put(COLUMN_TITLE, webSite.title);
                            values.put(COLUMN_FAVICON, webSite.faviconUrl);
                            values.put(COLUMN_CANONICAL, webSite.canonicalUrl);
                            values.put(COLUMN_COLOR, webSite.themeColor);
                            values.put(COLUMN_AMP, webSite.ampUrl);
                            values.put(COLUMN_BOOKMARKED, webSite.bookmarked);
                            values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
                            values.put(COLUMN_VISITED, 1);
                            if (database.insert(TABLE_NAME, null, values) != -1) {
                                return Observable.just(webSite);
                            } else {
                                return Observable.just(null);
                            }
                        }
                    }
                });
    }

    @NonNull
    @Override
    public Observable<WebSite> update(@NonNull WebSite webSite) {
        return get(webSite).flatMap(new Func1<WebSite, Observable<WebSite>>() {
            @Override
            public Observable<WebSite> call(WebSite saved) {
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
                        return Observable.just(webSite);
                    }
                } else {
                    return Observable.just(webSite);
                }
            }
        });
    }

    @NonNull
    @Override
    public Observable<WebSite> delete(@NonNull WebSite webSite) {
        return Observable.fromCallable(() -> {
            open();
            final String whereClause = COLUMN_URL + "=?";
            final String[] whereArgs = {webSite.url};
            if (database.delete(TABLE_NAME, whereClause, whereArgs) > 0) {
                Timber.d("Deletion successful for %s", webSite.url);
            } else {
                Timber.e("Deletion failed for %s", webSite.url);
            }
            return webSite;
        });
    }

    @NonNull
    @Override
    public Observable<Boolean> exists(@NonNull WebSite webSite) {
        return Observable.fromCallable(() -> {
            open();
            final String selection = " " + COLUMN_URL + "=?";
            final String[] selectionArgs = {webSite.url};
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
    public Observable<Boolean> deleteAll() {
        return null;
    }
}
