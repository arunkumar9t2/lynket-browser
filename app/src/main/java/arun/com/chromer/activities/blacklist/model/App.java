package arun.com.chromer.activities.blacklist.model;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Comparator;

import arun.com.chromer.util.Utils;

/**
 * Created by Arun on 24/01/2016.
 */
public class App implements Comparable {
    public String appName;
    public String packageName;
    public Drawable appIcon;
    public CharSequence label;
    public boolean blackListed;

    public App(Context context, String packageName) {
        this.packageName = packageName;
        this.appName = Utils.getAppNameWithPackage(context, packageName);
        try {
            this.appIcon = context.getApplicationContext().getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            this.appIcon = null;
        }
    }

    public App() {

    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public CharSequence getLabel() {
        return label;
    }

    public void setLabel(CharSequence label) {
        this.label = label;
    }

    public boolean isBlackListed() {
        return blackListed;
    }

    public void setBlackListed(boolean blackListed) {
        this.blackListed = blackListed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        App app = (App) o;
        return packageName != null ? packageName.equals(app.packageName) : app.packageName == null;

    }

    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return blackListAwareComparison(this, (App) another);
    }

    private static int blackListAwareComparison(@Nullable App lhs, @Nullable App rhs) {
        final String lhsName = lhs != null ? lhs.appName : null;
        final String rhsName = rhs != null ? rhs.appName : null;

        boolean lhsBlacklist = lhs != null && lhs.blackListed;
        boolean rhsBlacklist = rhs != null && rhs.blackListed;

        if (lhsBlacklist ^ rhsBlacklist) return (lhsBlacklist) ? -1 : 1;
        if (lhsName == null ^ rhsName == null) return lhs == null ? -1 : 1;
        //noinspection ConstantConditions
        if (lhsName == null && rhsName == null) return 0;

        return lhsName.compareToIgnoreCase(rhsName);
    }

    @SuppressWarnings("WeakerAccess")
    public static class AppComparator implements Comparator<App> {

        @Override
        public int compare(App lhs, App rhs) {
            return blackListAwareComparison(lhs, rhs);
        }
    }
}
