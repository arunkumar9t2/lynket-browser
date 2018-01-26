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

package arun.com.chromer.data.webarticle;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.qualifiers.Disk;
import arun.com.chromer.data.qualifiers.Network;
import arun.com.chromer.data.webarticle.model.WebArticle;
import arun.com.chromer.util.SchedulerProvider;
import rx.Observable;

/**
 * Website repository implementation for managing and providing website data.
 */
@Singleton
public class DefaultWebArticleRepository implements WebArticleRepository {
    private static final String TAG = DefaultWebArticleRepository.class.getSimpleName();
    @SuppressWarnings("FieldCanBeLocal")
    private final Context context;

    // Network store
    private final WebArticleStore articleNetworkStore;
    // Cache store
    private final WebArticleStore articleCacheStore;

    @Inject
    DefaultWebArticleRepository(@NonNull Application application,
                                @Network WebArticleStore articleNetworkStore,
                                @Disk WebArticleStore articleCacheStore) {
        this.context = application;
        this.articleNetworkStore = articleNetworkStore;
        this.articleCacheStore = articleCacheStore;
    }

    @NonNull
    @Override
    public Observable<WebArticle> getWebArticle(@NonNull final String url) {
        return articleCacheStore.getWebArticle(url)
                .flatMap(webArticle -> {
                    if (webArticle == null) {
                        Log.d(TAG, String.format("Cache miss for %s", url));
                        //noinspection Convert2MethodRef
                        return articleNetworkStore.getWebArticle(url)
                                .filter(webArticle1 -> webArticle1 != null)
                                .flatMap(articleCacheStore::saveWebArticle);
                    } else {
                        Log.d(TAG, String.format("Cache hit for %s", url));
                        return Observable.just(webArticle);
                    }
                }).compose(SchedulerProvider.applySchedulers());
    }
}
