package arun.com.chromer.data.apps;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;

import arun.com.chromer.data.common.App;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.Utils;
import io.paperdb.Book;
import io.paperdb.Paper;
import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

class AppDiskStore implements AppStore {
    private final Context context;

    private static final String APP_BOOK_NAME = "APPS";

    AppDiskStore(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Book getBook() {
        return Paper.book(APP_BOOK_NAME);
    }

    @NonNull
    @Override
    public Observable<App> getApp(@NonNull final String packageName) {
        return Observable.fromCallable(new Callable<App>() {
            @Override
            public App call() throws Exception {
                return getBook().read(packageName, null);
            }
        });
    }

    @NonNull
    @Override
    public Observable<App> savApp(App app) {
        return Observable.just(app)
                .flatMap(new Func1<App, Observable<App>>() {
                    @Override
                    public Observable<App> call(final App app) {
                        if (app == null) {
                            return null;
                        } else {
                            getBook().write(app.packageName, app);
                            Timber.d("Wrote %s to storage", app.packageName);
                            return Observable.just(app);
                        }
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
                .flatMap(new Func1<App, Observable<App>>() {
                    @Override
                    public Observable<App> call(App app) {
                        if (app != null) {
                            app.blackListed = true;
                            Timber.d("Set %s as blacklisted", app.packageName);
                            return savApp(app);
                        } else {
                            Timber.d("Added %s and blacklisted", packageName);
                            app = Utils.createApp(context, packageName);
                            app.blackListed = true;
                            return savApp(app);
                        }
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
                .map(new Func1<App, Integer>() {
                    @Override
                    public Integer call(final App app) {
                        if (app != null) {
                            Timber.d("Got %d color for %s from storage", app.color, app.packageName);
                            return app.color;
                        } else {
                            return Constants.NO_COLOR;
                        }
                    }
                });
    }

    @Override
    public Observable<App> setPackageColor(@NonNull final String packageName, final int color) {
        return getApp(packageName)
                .flatMap(new Func1<App, Observable<App>>() {
                    @Override
                    public Observable<App> call(final App app) {
                        if (app != null) {
                            app.color = color;
                            Timber.d("Saved %d color for %s", color, app.packageName);
                            return savApp(app);
                        } else {
                            Timber.d("Created and saved %d color for %s", color, packageName);
                            return savApp(Utils.createApp(context, packageName));
                        }
                    }
                });
    }

    @Override
    public Observable<App> removeBlacklist(final String packageName) {
        if (getBook().exist(packageName)) {
            return getApp(packageName)
                    .flatMap(new Func1<App, Observable<App>>() {
                        @Override
                        public Observable<App> call(App app) {
                            if (app != null) {
                                app.blackListed = false;
                                Timber.d("Blacklist removed %s", app.packageName);
                                return savApp(app);
                            } else {
                                return Observable.just(null);
                            }
                        }
                    });
        } else {
            return Observable.just(null);
        }
    }


}
