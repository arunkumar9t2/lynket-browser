package arun.com.chromer.activities.blacklist;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Callable;

import arun.com.chromer.activities.blacklist.model.App;
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
        WeakReference<View> viewRef;
        private final CompositeSubscription compositeSubscription = new CompositeSubscription();

        Presenter(@NonNull View view) {
            viewRef = new WeakReference<>(view);
        }

        boolean isViewAttached() {
            return viewRef != null && viewRef.get() != null;
        }

        View getView() {
            return viewRef.get();
        }

        void loadAppList(@NonNull final Context context) {
            if (isViewAttached()) {
                getView().setRefreshing(true);
            }
            final PackageManager pm = context.getApplicationContext().getPackageManager();
            final Subscription subscription = Observable.fromCallable(new Callable<List<ResolveInfo>>() {
                @Override
                public List<ResolveInfo> call() throws Exception {
                    final Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    return pm.queryIntentActivities(intent, 0);
                }
            }).flatMap(new Func1<List<ResolveInfo>, Observable<ResolveInfo>>() {
                @Override
                public Observable<ResolveInfo> call(List<ResolveInfo> resolveInfos) {
                    return Observable.from(resolveInfos);
                }
            }).filter(new Func1<ResolveInfo, Boolean>() {
                @Override
                public Boolean call(ResolveInfo resolveInfo) {
                    return resolveInfo != null && !resolveInfo.activityInfo.packageName.equalsIgnoreCase(context.getPackageName());
                }
            }).map(new Func1<ResolveInfo, App>() {
                @Override
                public App call(ResolveInfo resolveInfo) {
                    final App app = new App();
                    final String pkg = resolveInfo.activityInfo.packageName;
                    app.setAppName(Utils.getAppNameWithPackage(context, pkg));
                    app.setPackageName(pkg);
                    app.setBlackListed(BlackListManager.isPackageBlackListed(pkg));
                    return app;
                }
            }).distinct()
                    .toSortedList()
                    .compose(RxUtils.<List<App>>applySchedulers())
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
            if (viewRef != null) {
                viewRef.clear();
                viewRef = null;
            }
            compositeSubscription.clear();
        }

        void updateBlacklist(@Nullable App app) {
            if (app != null && app.getPackageName() != null) {
                if (app.isBlackListed()) {
                    BlackListManager.setBlackListed(app.getPackageName());
                } else {
                    BlackListManager.deleteBlackListed(app.getPackageName());
                }
            }
        }
    }
}
