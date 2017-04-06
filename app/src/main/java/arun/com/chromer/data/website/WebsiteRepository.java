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

package arun.com.chromer.data.website;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import arun.com.chromer.data.history.HistoryRepository;
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
    private final WebsiteStore diskStore;

    private WebsiteRepository(@NonNull Context context) {
        this.context = context.getApplicationContext();
        webNetworkStore = new WebsiteNetworkStore(context);
        diskStore = new WebsiteDiskStore(context);
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
        final Observable<WebSite> cache = diskStore.getWebsite(url)
                .doOnNext(webSite -> {
                    if (webSite != null) {
                        HistoryRepository.getInstance(context).update(webSite).subscribe();
                    }
                });

        final Observable<WebSite> history = HistoryRepository.getInstance(context).get(new WebSite(url))
                .doOnNext(webSite -> {
                    if (webSite != null) {
                        HistoryRepository.getInstance(context).update(webSite).subscribe();
                    }
                });

        final Observable<WebSite> remote = webNetworkStore.getWebsite(url)
                .filter(webSite -> webSite != null)
                .doOnNext(webSite -> {
                    diskStore.saveWebsite(webSite).subscribe();
                    HistoryRepository.getInstance(context).insert(webSite).subscribe();
                });

        return Observable.concat(cache, history, remote)
                .first(webSite -> webSite != null)
                .doOnError(Timber::e)
                .compose(RxUtils.applySchedulers());
    }

    @Override
    public int getWebsiteColorSync(@NonNull String url) {
        return diskStore.getWebsiteColor(url).toBlocking().first().color;
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
                            return diskStore.saveWebsiteColor(Uri.parse(webSite.url).getHost(), webSite.themeColor());
                        } else {
                            return Observable.empty();
                        }
                    }
                });
    }

    @NonNull
    @Override
    public Observable<Void> clearCache() {
        return diskStore.clearCache();
    }

}
