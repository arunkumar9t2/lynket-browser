package arun.com.chromer.shared;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import arun.com.chromer.activities.settings.Preferences;
import timber.log.Timber;

/**
 * Created by Arunkumar on 21-01-2017.
 */
public final class AppDetectionManager {
    // Last detected package name of app;
    private String filteredPackage = "";
    private String nonFilteredPackage = "";

    private final Context context;

    @SuppressLint("StaticFieldLeak")
    // But this is the recommended way as per developer training :/ :/
    private static AppDetectionManager INSTANCE;

    private AppDetectionManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized static AppDetectionManager getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new AppDetectionManager(context);
        }
        return INSTANCE;
    }


    synchronized void logPackage(@Nullable String appPackage) {
        if (TextUtils.isEmpty(appPackage)) {
            return;
        }
        if (!nonFilteredPackage.equalsIgnoreCase(appPackage) && nonFilterPackage(appPackage)) {
            nonFilteredPackage = appPackage;
        }
        if (!filteredPackage.equalsIgnoreCase(appPackage) && filterPackage(appPackage)) {
            filteredPackage = appPackage;
        }
        Timber.d("Current package: %s", appPackage);
    }

    public synchronized String getNonFilteredPackage() {
        return nonFilteredPackage;
    }

    public synchronized String getFilteredPackage() {
        return filteredPackage;
    }

    public synchronized void clear() {
        filteredPackage = nonFilteredPackage = "";
    }


    private boolean nonFilterPackage(@NonNull String appPackage) {
        // Ignore system pop ups
        if (appPackage.equalsIgnoreCase("android")) return false;

        // Ignore our app
        return !appPackage.equalsIgnoreCase(context.getPackageName());
    }

    private boolean filterPackage(@NonNull String packageName) {
        // Ignore system pop ups
        if (packageName.equalsIgnoreCase("android")) return false;

        // Ignore our app
        if (packageName.equalsIgnoreCase(context.getPackageName())) return false;

        // Chances are that we picked the opening custom tab, so let's ignore our default provider
        // to be safe
        if (packageName.equalsIgnoreCase(Preferences.get(context).customTabApp())) return false;

        // Ignore google quick search box
        if (packageName.equalsIgnoreCase("com.google.android.googlequicksearchbox")) return false;

        // There can also be cases where there is no default provider set, so lets ignore all possible
        // custom tab providers to be sure. This is safe since browsers don't call our app anyways.

        // Commenting, research needed
        // if (mCustomTabPackages.contains(packageName)) return true;

        return true;
    }
}
