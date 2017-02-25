package arun.com.chromer.data.website;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;

import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.util.cache.ParcelDiskCache;
import rx.Observable;
import timber.log.Timber;

/**
 * Cache store to get/put {@link WebSite} objects to disk cache.
 */
class WebsiteCacheStore implements WebsiteStore {
    @SuppressWarnings("FieldCanBeLocal")
    private final Context context;
    // Cache to store our data.
    private ParcelDiskCache<WebSite> webSiteDiskCache;
    // Cache size, currently set at 30 MB.
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 30;

    WebsiteCacheStore(Context context) {
        this.context = context.getApplicationContext();
        try {
            webSiteDiskCache = ParcelDiskCache.open(context, WebSite.class.getClassLoader(), "WebSiteCache", DISK_CACHE_SIZE);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    @NonNull
    @Override
    public Observable<WebSite> getWebsite(@NonNull final String url) {
        return Observable.fromCallable(() -> webSiteDiskCache.get(url.trim()));
    }

    @NonNull
    @Override
    public Observable<WebSite> saveWebsite(@NonNull final WebSite webSite) {
        return Observable.fromCallable(() -> webSiteDiskCache.set(webSite.url, webSite));
    }
}
