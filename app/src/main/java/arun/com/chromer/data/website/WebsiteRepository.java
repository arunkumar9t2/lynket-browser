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

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.history.BaseHistoryRepository;
import arun.com.chromer.data.qualifiers.Disk;
import arun.com.chromer.data.qualifiers.Network;
import arun.com.chromer.data.website.model.WebColor;
import arun.com.chromer.data.website.model.WebSite;
import arun.com.chromer.util.RxUtils;
import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.NO_COLOR;

/**
 * Website repository implementation for managing and providing website data.
 */
@Singleton
public class WebsiteRepository implements BaseWebsiteRepository {
    private final Context context;
    private final WebsiteStore webNetworkStore;
    private final WebsiteStore diskStore;
    private final BaseHistoryRepository historyRepository;

    @Inject
    WebsiteRepository(@NonNull Application context, @Disk WebsiteStore diskStore, @Network WebsiteStore webNetworkStore, BaseHistoryRepository historyRepository) {
        this.context = context.getApplicationContext();
        this.webNetworkStore = webNetworkStore;
        this.diskStore = diskStore;
        this.historyRepository = historyRepository;
    }

    @NonNull
    @Override
    public Observable<WebSite> getWebsite(@NonNull final String url) {
        final Observable<WebSite> cache = diskStore.getWebsite(url)
                .doOnNext(webSite -> {
                    if (webSite != null) {
                        historyRepository.update(webSite).subscribe();
                    }
                });

        final Observable<WebSite> history = historyRepository.get(new WebSite(url))
                .doOnNext(webSite -> {
                    if (webSite != null) {
                        historyRepository.update(webSite).subscribe();
                    }
                });

        final Observable<WebSite> remote = webNetworkStore.getWebsite(url)
                .filter(webSite -> webSite != null)
                .doOnNext(webSite -> {
                    diskStore.saveWebsite(webSite).subscribe();
                    historyRepository.insert(webSite).subscribe();
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
                .flatMap(webSite -> {
                    if (webSite != null && webSite.themeColor() != NO_COLOR) {
                        return diskStore.saveWebsiteColor(Uri.parse(webSite.url).getHost(), webSite.themeColor());
                    } else {
                        return Observable.empty();
                    }
                });
    }

    @NonNull
    @Override
    public Observable<Void> clearCache() {
        return diskStore.clearCache();
    }

}
