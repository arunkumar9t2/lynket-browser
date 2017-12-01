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
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.customtabs.dynamictoolbar.AppColorExtractorJob;
import arun.com.chromer.data.apps.store.AppStore;
import arun.com.chromer.data.common.App;
import arun.com.chromer.shared.Constants;
import rx.Observable;
import timber.log.Timber;

@Singleton
public class AppRepository implements BaseAppRepository {
    private final Context application;
    // Disk app store
    private final AppStore diskStore;

    @Inject
    AppRepository(@NonNull Application application, @NonNull AppStore appStore) {
        this.application = application;
        this.diskStore = appStore;
    }

    @NonNull
    @Override
    public Observable<App> getApp(@NonNull String packageName) {
        return diskStore.getApp(packageName);
    }

    @NonNull
    @Override
    public Observable<App> savApp(App app) {
        return diskStore.savApp(app);
    }

    @Override
    public Observable<Integer> getPackageColor(@NonNull final String packageName) {
        return diskStore.getPackageColor(packageName)
                .doOnNext(integer -> {
                    if (integer == -1) {
                        Timber.d("Color not found, starting extraction.");
                        AppColorExtractorJob.enqueueWork(application, AppColorExtractorJob.class, AppColorExtractorJob.JOB_ID, new Intent().putExtra(Constants.EXTRA_PACKAGE_NAME, packageName));
                    }
                });
    }

    @Override
    public Observable<App> setPackageColor(@NonNull String packageName, int color) {
        return diskStore.setPackageColor(packageName, color);
    }

    @Override
    public Observable<App> removeBlacklist(String packageName) {
        return diskStore.removeBlacklist(packageName);
    }

    @Override
    public boolean isPackageBlacklisted(@NonNull String packageName) {
        return diskStore.isPackageBlacklisted(packageName);
    }

    @Override
    public Observable<App> setPackageBlacklisted(@NonNull String packageName) {
        return diskStore.setPackageBlacklisted(packageName);
    }

    @Override
    public int getPackageColorSync(@NonNull String packageName) {
        return getPackageColor(packageName).toBlocking().first();
    }
}
