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

package arun.com.chromer.data.apps;

import android.app.Application;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.data.apps.store.AppStore;
import arun.com.chromer.data.common.App;
import arun.com.chromer.data.common.BookStore;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.Utils;
import io.paperdb.Book;
import io.paperdb.Paper;
import rx.Observable;
import timber.log.Timber;

@Singleton
public class AppDiskStore implements AppStore, BookStore {
    private final Application application;

    private static final String APP_BOOK_NAME = "APPS";

    @Inject
    AppDiskStore(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public Book getBook() {
        return Paper.book(APP_BOOK_NAME);
    }

    @NonNull
    @Override
    public Observable<App> getApp(@NonNull final String packageName) {
        return Observable.fromCallable(() -> getBook().read(packageName, null));
    }

    @NonNull
    @Override
    public Observable<App> savApp(App app) {
        return Observable.just(app)
                .flatMap(app1 -> {
                    if (app1 == null) {
                        return null;
                    } else {
                        getBook().write(app1.packageName, app1);
                        Timber.d("Wrote %s to storage", app1.packageName);
                        return Observable.just(app1);
                    }
                });
    }

    @Override
    public boolean isPackageBlacklisted(@NonNull String packageName) {
        return getBook().exist(packageName)
                && getApp(packageName).toBlocking().first().blackListed;
    }

    @Override
    public Observable<App> setPackageBlacklisted(@NonNull final String packageName) {
        return getApp(packageName)
                .flatMap(app -> {
                    if (app != null) {
                        app.blackListed = true;
                        Timber.d("Set %s as blacklisted", app.packageName);
                        return savApp(app);
                    } else {
                        Timber.d("Added %s and blacklisted", packageName);
                        app = Utils.createApp(application, packageName);
                        app.blackListed = true;
                        return savApp(app);
                    }
                });
    }

    @Override
    public int getPackageColorSync(@NonNull String packageName) {
        return getApp(packageName).toBlocking().first().color;
    }

    @Override
    public Observable<Integer> getPackageColor(@NonNull String packageName) {
        return getApp(packageName)
                .map(app -> {
                    if (app != null) {
                        Timber.d("Got %d color for %s from storage", app.color, app.packageName);
                        return app.color;
                    } else {
                        return Constants.NO_COLOR;
                    }
                });
    }

    @Override
    public Observable<App> setPackageColor(@NonNull final String packageName, final int color) {
        return getApp(packageName)
                .flatMap(app -> {
                    if (app != null) {
                        app.color = color;
                        Timber.d("Saved %d color for %s", color, app.packageName);
                        return savApp(app);
                    } else {
                        Timber.d("Created and saved %d color for %s", color, packageName);
                        return savApp(Utils.createApp(application, packageName));
                    }
                });
    }

    @Override
    public Observable<App> removeBlacklist(final String packageName) {
        if (getBook().exist(packageName)) {
            return getApp(packageName)
                    .flatMap(app -> {
                        if (app != null) {
                            app.blackListed = false;
                            Timber.d("Blacklist removed %s", app.packageName);
                            return savApp(app);
                        } else {
                            return Observable.just(null);
                        }
                    });
        } else {
            return Observable.just(null);
        }
    }
}
