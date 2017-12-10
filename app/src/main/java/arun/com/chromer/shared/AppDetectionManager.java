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

package arun.com.chromer.shared;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

import arun.com.chromer.activities.settings.Preferences;
import timber.log.Timber;

/**
 * Created by Arunkumar on 21-01-2017.
 */
@Singleton
public final class AppDetectionManager {
    private final Context context;

    // Last detected package name of app;
    private String filteredPackage = "";
    private String nonFilteredPackage = "";

    @Inject
    public AppDetectionManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
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
        if (packageName.equalsIgnoreCase(Preferences.get(context).customTabPackage())) return false;

        // Ignore google quick search box
        if (packageName.equalsIgnoreCase("com.google.android.googlequicksearchbox")) return false;

        // There can also be cases where there is no default provider set, so lets ignore all possible
        // custom tab providers to be sure. This is safe since browsers don't call our app anyways.

        // Commenting, research needed
        // if (mCustomTabPackages.contains(packageName)) return true;

        return true;
    }
}
