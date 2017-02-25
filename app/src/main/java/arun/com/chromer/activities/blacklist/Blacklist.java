package arun.com.chromer.activities.blacklist;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;

import arun.com.chromer.data.apps.AppRepository;
import arun.com.chromer.data.common.App;
import arun.com.chromer.util.RxUtils;
import arun.com.chromer.util.Utils;
import rx.Observable;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Blacklist screen interface holding View and Presenter classes.
 */
interface Blacklist {
    interface View {
        void setApps(@NonNull List<App> apps);

        void setRefreshing(boolean refreshing);
    }

    /**
     * Presenter containing all business logic for this screen.
     */
    class Presenter {
        final WeakReference<View> viewRef;
        private final CompositeSubscription compositeSubscription = new CompositeSubscription();

        Presenter(@NonNull View view) {
            viewRef = new WeakReference<>(view);
        }

        boolean isViewAttached() {
            return viewRef.get() != null;
        }

        @NonNull
        View getView() {
            return viewRef.get();
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
                    .flatMap(new Func1<List<ResolveInfo>, Observable<ResolveInfo>>() {
                        @Override
                        public Observable<ResolveInfo> call(List<ResolveInfo> resolveInfos) {
                            return Observable.from(resolveInfos);
                        }
                    }).filter(resolveInfo -> resolveInfo != null
                            && !resolveInfo.activityInfo.packageName.equalsIgnoreCase(context.getPackageName()))
                    .map(resolveInfo -> {
                        final App app = Utils.createApp(context, resolveInfo.activityInfo.packageName);
                        app.blackListed = AppRepository.getInstance(context).isPackageBlacklisted(app.packageName);
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
            compositeSubscription.add(subscription);
        }

        void cleanUp() {
            viewRef.clear();
            compositeSubscription.clear();
        }

        void updateBlacklist(@NonNull Context context, @Nullable App app) {
            if (app != null) {
                if (app.blackListed) {
                    AppRepository.getInstance(context)
                            .setPackageBlacklisted(app.packageName)
                            .compose(RxUtils.applySchedulers())
                            .subscribe();
                } else {
                    AppRepository.getInstance(context)
                            .removeBlacklist(app.packageName)
                            .compose(RxUtils.applySchedulers())
                            .subscribe();
                }
            }
        }
    }
}
