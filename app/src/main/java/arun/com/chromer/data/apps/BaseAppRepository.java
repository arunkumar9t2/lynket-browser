package arun.com.chromer.data.apps;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import arun.com.chromer.data.common.App;
import rx.Observable;

interface BaseAppRepository {
    @NonNull
    Observable<App> getApp(@NonNull String packageName);

    @NonNull
    Observable<App> savApp(App app);

    boolean isPackageBlacklisted(@NonNull String packageName);

    Observable<App> setPackageBlacklisted(@NonNull String packageName);

    @ColorInt
    int getPackageColorSync(@NonNull String packageName);

    Observable<Integer> getPackageColor(@NonNull String packageName);

    Observable<App> setPackageColor(@NonNull String packageName, int color);

    Observable<App> removeBlacklist(String packageName);
}
