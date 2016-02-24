package arun.com.chromer.model;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.Comparator;

import arun.com.chromer.util.Util;

/**
 * Created by Arun on 24/01/2016.
 */
public class App implements Comparable {
    private String appName;
    private String packageName;
    private Drawable appIcon;
    private CharSequence label;
    private boolean blackListed;

    public App(Context context, String packageName) {
        this.packageName = packageName;
        this.appName = Util.getAppNameWithPackage(context, packageName);
        try {
            this.appIcon = context.getPackageManager().getApplicationIcon(packageName);
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
    public int compareTo(Object another) {
        return compareApps(this, (App) another);
    }

    public static class AppComparator implements Comparator<App> {

        @Override
        public int compare(App lhs, App rhs) {
            return compareApps(lhs, rhs);
        }
    }

    private static int compareApps(App lhs, App rhs) {
        String lhsName = lhs != null ? lhs.appName : null;
        String rhsName = rhs != null ? rhs.appName : null;

        boolean lhsBlacklist = lhs != null && lhs.blackListed;
        boolean rhsBlacklist = rhs != null && rhs.blackListed;

        if (lhsBlacklist ^ rhsBlacklist) return (lhsBlacklist) ? -1 : 1;

        if (lhsName == null ^ rhsName == null) return lhs == null ? -1 : 1;

        if (lhsName == null && rhsName == null) return 0;

        return lhsName.compareToIgnoreCase(rhsName);
    }
}
