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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.history.HistoryRepository;
import arun.com.chromer.data.qualifiers.Disk;
import arun.com.chromer.data.qualifiers.Network;
import arun.com.chromer.data.website.model.WebColor;
import arun.com.chromer.data.website.model.Website;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.SchedulerProvider;
import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.NO_COLOR;

/**
 * Website repository implementation for managing and providing website data.
 */
@Singleton
public class DefaultWebsiteRepository implements WebsiteRepository {
    private final Context context;
    private final WebsiteStore webNetworkStore;
    private final WebsiteStore diskStore;
    private final HistoryRepository historyRepository;

    @Inject
    DefaultWebsiteRepository(@NonNull Application context, @Disk WebsiteStore diskStore, @Network WebsiteStore webNetworkStore, HistoryRepository historyRepository) {
        this.context = context.getApplicationContext();
        this.webNetworkStore = webNetworkStore;
        this.diskStore = diskStore;
        this.historyRepository = historyRepository;
    }

    @NonNull
    @Override
    public Observable<Website> getWebsite(@NonNull final String url) {
        final Observable<Website> cache = diskStore.getWebsite(url)
                .doOnNext(webSite -> {
                    if (webSite != null) {
                        historyRepository.update(webSite).subscribe();
                    }
                });

        final Observable<Website> history = historyRepository.get(new Website(url))
                .doOnNext(webSite -> {
                    if (webSite != null) {
                        historyRepository.update(webSite).subscribe();
                    }
                });

        final Observable<Website> remote = webNetworkStore.getWebsite(url)
                .filter(webSite -> webSite != null)
                .doOnNext(webSite -> {
                    diskStore.saveWebsite(webSite).subscribe();
                    historyRepository.insert(webSite).subscribe();
                });

        return Observable.concat(cache, history, remote)
                .first(webSite -> webSite != null)
                .doOnError(Timber::e)
                .compose(SchedulerProvider.applyIoSchedulers());
    }

    @Override
    public int getWebsiteColorSync(@NonNull String url) {
        return diskStore.getWebsiteColor(url)
                .map(webColor -> {
                    if (webColor.color == Constants.NO_COLOR) {
                        saveWebColor(url).subscribe();
                    }
                    return webColor;
                })
                .toBlocking()
                .first()
                .color;
    }

    @NonNull
    @Override
    public Observable<WebColor> saveWebColor(String url) {
        return getWebsite(url)
                .observeOn(Schedulers.io())
                .flatMap(webSite -> {
                    if (webSite != null) {
                        if (webSite.themeColor() != NO_COLOR) {
                            int color = webSite.themeColor();
                            return diskStore.saveWebsiteColor(Uri.parse(webSite.url).getHost(), color);
                        } else {
                            final int color = getWebsiteIconAndColor(webSite).second;
                            if (color != Constants.NO_COLOR) {
                                return diskStore.saveWebsiteColor(Uri.parse(webSite.url).getHost(), color);
                            } else return Observable.empty();
                        }
                    } else return Observable.empty();
                });
    }

    @NonNull
    @Override
    public Observable<Void> clearCache() {
        return diskStore.clearCache();
    }

    @NonNull
    @Override
    public Pair<Bitmap, Integer> getWebsiteIconAndColor(@NonNull Website website) {
        return webNetworkStore.getWebsiteIconAndColor(website);
    }

    @NonNull
    @Override
    public Pair<Drawable, Integer> getWebsiteFaviconAndColor(@NonNull Website website) {
        return webNetworkStore.getWebsiteRoundIconAndColor(website);
    }
}
