package arun.com.chromer.data.website;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import arun.com.chromer.data.website.model.WebColor;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.util.RxUtils;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.NO_COLOR;

/**
 * Website repository implementation for managing and providing website data.
 */
public class WebsiteRepository implements BaseWebsiteRepository {
    @SuppressWarnings("FieldCanBeLocal")
    private final Context context;
    // Singleton instance
    @SuppressLint("StaticFieldLeak")
    private static WebsiteRepository INSTANCE;
    // Network store
    private final WebsiteStore webNetworkStore;
    // Cache store
    private final WebsiteStore cacheStore;

    private WebsiteRepository(@NonNull Context context) {
        this.context = context.getApplicationContext();
        webNetworkStore = new WebsiteNetworkStore(context);
        cacheStore = new WebsiteDiskStore(context);
    }

    public static synchronized WebsiteRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new WebsiteRepository(context);
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public Observable<WebSite> getWebsite(@NonNull final String url) {
        return cacheStore.getWebsite(url)
                .flatMap(new Func1<WebSite, Observable<WebSite>>() {
                    @Override
                    public Observable<WebSite> call(WebSite webSite) {
                        if (webSite == null) {
                            Timber.d("Cache miss for: %s", url);
                            return webNetworkStore.getWebsite(url)
                                    .filter(webSite1 -> webSite1 != null)
                                    .flatMap(new Func1<WebSite, Observable<WebSite>>() {
                                        @Override
                                        public Observable<WebSite> call(WebSite webSite) {
                                            return cacheStore.saveWebsite(webSite);
                                        }
                                    });
                        } else {
                            Timber.d("Cache hit for : %s", url);
                            return Observable.just(webSite);
                        }
                    }
                }).doOnError(Timber::e)
                .compose(RxUtils.applySchedulers());
    }

    @Override
    public int getWebsiteColorSync(@NonNull String url) {
        return cacheStore.getWebsiteColor(url).toBlocking().first().color;
    }

    @NonNull
    @Override
    public Observable<WebColor> saveWebColor(String url) {
        return getWebsite(url)
                .observeOn(Schedulers.io())
                .flatMap(new Func1<WebSite, Observable<WebColor>>() {
                    @Override
                    public Observable<WebColor> call(WebSite webSite) {
                        if (webSite != null && webSite.themeColor() != NO_COLOR) {
                            return cacheStore.saveWebsiteColor(Uri.parse(webSite.url).getHost(), webSite.themeColor());
                        } else {
                            return Observable.empty();
                        }
                    }
                });
    }

}
