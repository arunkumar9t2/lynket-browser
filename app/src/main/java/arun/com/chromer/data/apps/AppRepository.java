package arun.com.chromer.data.apps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import arun.com.chromer.customtabs.dynamictoolbar.AppColorExtractorService;
import arun.com.chromer.data.common.App;
import arun.com.chromer.shared.Constants;
import rx.Observable;
import timber.log.Timber;

public class AppRepository implements BaseAppRepository {
    private final Context context;
    // Disk app store
    private final AppDiskStore diskStore;
    @SuppressLint("StaticFieldLeak")
    private static AppRepository INSTANCE;

    /**
     * Used to get a singleton instance of the repository
     *
     * @param context Context to work with.
     * @return Instance of this class.
     */
    public static AppRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppRepository(context);
        }
        return INSTANCE;
    }

    private AppRepository(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.diskStore = new AppDiskStore(context);
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
                        final Intent intent = new Intent(context, AppColorExtractorService.class);
                        intent.putExtra(Constants.EXTRA_PACKAGE_NAME, packageName);
                        context.startService(intent);
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
