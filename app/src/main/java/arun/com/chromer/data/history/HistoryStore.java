package arun.com.chromer.data.history;

import android.database.Cursor;
import android.support.annotation.NonNull;

import arun.com.chromer.data.website.model.WebSite;
import rx.Observable;

/**
 * Created by Arunkumar on 03-03-2017.
 */
public interface HistoryStore {
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
    Observable<Boolean> deleteAll();
}
