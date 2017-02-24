package arun.com.chromer.data.website;

import android.support.annotation.NonNull;

import arun.com.chromer.data.website.model.WebSite;
import rx.Observable;

/**
 * Created by arunk on 24-02-2017.
 */

interface WebSiteStore {
    @NonNull
    Observable<WebSite> getWebsite(@NonNull String url);

    @NonNull
    Observable<WebSite> saveWebsite(@NonNull WebSite webSite);
}
