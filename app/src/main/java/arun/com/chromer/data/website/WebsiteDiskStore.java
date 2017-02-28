package arun.com.chromer.data.website;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import java.io.IOException;

import arun.com.chromer.data.common.BookStore;
import arun.com.chromer.data.website.model.WebColor;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.util.cache.ParcelDiskCache;
import io.paperdb.Book;
import io.paperdb.Paper;
import rx.Observable;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.NO_COLOR;

/**
 * Cache store to get/put {@link WebSite} objects to disk cache.
 */
class WebsiteDiskStore implements WebsiteStore, BookStore {
    @SuppressWarnings("FieldCanBeLocal")
    private final Context context;
    // Cache to store our data.
    private ParcelDiskCache<WebSite> webSiteDiskCache;
    // Cache size, currently set at 30 MB.
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 30;

    private static final String THEME_COLOR_BOOK = "THEME_COLOR_BOOK";

    @Override
    public Book getBook() {
        return Paper.book(THEME_COLOR_BOOK);
    }

    WebsiteDiskStore(Context context) {
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
        return Observable.fromCallable(() -> webSiteDiskCache.set(webSite.url, webSite))
                .doOnNext(cachedWebsite -> {

                });
    }

    @NonNull
    @Override
    public Observable<WebColor> getWebsiteColor(@NonNull final String url) {
        return Observable.fromCallable(() -> {
            try {
                return (WebColor) getBook().read(Uri.parse(url).getHost());
            } catch (Exception e) {
                Timber.e(e);
                return null;
            }
        }).map(webColor -> {
            if (webColor == null) {
                return new WebColor(Uri.parse(url).getHost(), NO_COLOR);
            } else return webColor;
        });
    }

    @Override
    public Observable<WebColor> saveWebsiteColor(@NonNull String host, @ColorInt int color) {
        return Observable.fromCallable(() -> {
            try {
                return getBook().write(host, new WebColor(host, color)).read(host);
            } catch (Exception e) {
                Timber.e(e);
                return new WebColor(host, NO_COLOR);
            }
        });
    }
}
