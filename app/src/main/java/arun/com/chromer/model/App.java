package arun.com.chromer.model;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import arun.com.chromer.util.Util;

/**
 * Created by Arun on 24/01/2016.
 */
public class App {
    private String appName;
    private String packageName;
    private Drawable appIcon;
    private CharSequence label;

    public App(Context context, String packageName) {
        this.packageName = packageName;
        this.appName = Util.getAppNameWithPackage(context, packageName);
        try {
            this.appIcon = context.getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            this.appIcon = null;
        }
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
}
