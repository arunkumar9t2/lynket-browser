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

package arun.com.chromer.activities.blacklist;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import arun.com.chromer.data.apps.BaseAppRepository;
import arun.com.chromer.data.common.App;
import arun.com.chromer.di.PerActivity;
import arun.com.chromer.shared.common.Base;
import arun.com.chromer.util.RxUtils;
import arun.com.chromer.util.Utils;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscription;
import timber.log.Timber;

/**
 * Blacklist screen interface holding View and Presenter classes.
 */
interface Blacklist {
    interface View extends Base.View {
        void setApps(@NonNull List<App> apps);

        void setRefreshing(boolean refreshing);
    }

    @PerActivity
    class Presenter extends Base.Presenter<View> {

        private final BaseAppRepository appRepository;

        @Inject
        public Presenter(@NonNull BaseAppRepository appRepository) {
            this.appRepository = appRepository;
        }

        void loadAppList(@NonNull final Context context) {
            if (isViewAttached()) {
                getView().setRefreshing(true);
            }
            final Comparator<App> appComparator = new App.BlackListComparator();
            final PackageManager pm = context.getApplicationContext().getPackageManager();
            final Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

            final Subscription subscription = Observable
                    .fromCallable(() -> pm.queryIntentActivities(intent, 0))
                    .flatMapIterable(resolveInfos -> resolveInfos)
                    .filter(resolveInfo -> resolveInfo != null
                            && !resolveInfo.activityInfo.packageName.equalsIgnoreCase(context.getPackageName()))
                    .map(resolveInfo -> {
                        final App app = Utils.createApp(context, resolveInfo.activityInfo.packageName);
                        app.blackListed = appRepository.isPackageBlacklisted(app.packageName);
                        return app;
                    }).distinct()
                    .toSortedList(appComparator::compare)
                    .compose(RxUtils.applySchedulers())
                    .toSingle()
                    .subscribe(new SingleSubscriber<List<App>>() {
                        @Override
                        public void onSuccess(List<App> apps) {
                            if (isViewAttached()) {
                                getView().setRefreshing(false);
                                getView().setApps(apps);
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            Timber.e(error.toString());
                        }
                    });
            subs.add(subscription);
        }

        void updateBlacklist(@Nullable App app) {
            if (app != null) {
                if (app.blackListed) {
                    appRepository.setPackageBlacklisted(app.packageName)
                            .compose(RxUtils.applySchedulers())
                            .subscribe();
                } else {
                    appRepository.removeBlacklist(app.packageName)
                            .compose(RxUtils.applySchedulers())
                            .subscribe();
                }
            }
        }

        void handleSelections(Observable<App> clicks) {
            subs.add(clicks.subscribe(this::updateBlacklist));
        }

        @Override
        public void onResume() {

        }

        @Override
        public void onPause() {

        }
    }
}
