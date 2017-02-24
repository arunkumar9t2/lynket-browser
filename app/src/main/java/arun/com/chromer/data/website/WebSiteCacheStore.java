package arun.com.chromer.data.website;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.Callable;

import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.util.cache.ParcelDiskCache;
import rx.Observable;

/**
 * Cache store to get/put {@link WebSite} objects to disk cache.
 */
class WebSiteCacheStore implements WebSiteStore {
    @SuppressWarnings("FieldCanBeLocal")
    private final Context context;
    // Cache to store our data.
    private ParcelDiskCache<WebSite> webSiteDiskCache;
    // Cache size, currently set at 30 Mb.
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 30;

    WebSiteCacheStore(Context context) {
        this.context = context.getApplicationContext();
        try {
            webSiteDiskCache = ParcelDiskCache.open(context, WebSite.class.getClassLoader(), "WebSiteCache", DISK_CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Observable<WebSite> getWebsite(@NonNull final String url) {
        return Observable.fromCallable(new Callable<WebSite>() {
            @Override
            public WebSite call() throws Exception {
                return webSiteDiskCache.get(url.trim());
            }
        });
    }

    @NonNull
    @Override
    public Observable<WebSite> saveWebsite(@NonNull final WebSite webSite) {
        return Observable.fromCallable(new Callable<WebSite>() {
            @Override
            public WebSite call() throws Exception {
                return webSiteDiskCache.set(webSite.url, webSite);
            }
        });
    }
}
