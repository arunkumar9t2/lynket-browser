package arun.com.chromer.customtabs;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import arun.com.chromer.R;
import arun.com.chromer.util.Util;
import timber.log.Timber;

// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Helper class for Custom Tabs.
 */
public class CustomTabHelper {
    public static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";
    public static final String STABLE_PACKAGE = "com.android.chrome";
    public static final String BETA_PACKAGE = "com.chrome.beta";
    public static final String DEV_PACKAGE = "com.chrome.dev";
    private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";

    public final static CustomTabBindingHelper.CustomTabsFallback CUSTOM_TABS_FALLBACK =
            new CustomTabBindingHelper.CustomTabsFallback() {
                @Override
                public void openUri(Activity activity, Uri uri) {
                    if (activity != null) {
                        Toast.makeText(activity,
                                activity.getString(R.string.fallback_msg),
                                Toast.LENGTH_SHORT).show();
                        try {
                            activity.startActivity(Intent.createChooser(
                                    new Intent(Intent.ACTION_VIEW, uri),
                                    activity.getString(R.string.open_with)));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(activity,
                                    activity.getString(R.string.unxp_err), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                }
            };

    private static final String EXTRA_CUSTOM_TABS_KEEP_ALIVE = "android.support.customtabs.extra.KEEP_ALIVE";
    private static String sPackageNameToUse;

    private CustomTabHelper() {
    }

    public static void addKeepAliveExtra(Context context, Intent intent) {
        Intent keepAliveIntent = new Intent().setClassName(
                context.getPackageName(),
                KeepAliveService.class.getCanonicalName());
        intent.putExtra(EXTRA_CUSTOM_TABS_KEEP_ALIVE, keepAliveIntent);
    }

    /**
     * Goes through all apps that handle VIEW intents and have a warmup service. Picks
     * the one chosen by the user if there is one, otherwise makes a best effort to return a
     * valid package name.
     * <p>
     * This is <strong>not</strong> threadsafe.
     *
     * @param context {@link Context} to use for accessing {@link PackageManager}.
     * @return The package name recommended to use for connecting to custom tabs related components.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static String getPackageNameToUse(Context context) {
        if (sPackageNameToUse != null && Util.isPackageInstalled(context, sPackageNameToUse)) {
            return sPackageNameToUse;
        }

        PackageManager pm = context.getApplicationContext().getPackageManager();
        // Get default VIEW intent handler.
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
        }

        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        if (packagesSupportingCustomTabs.isEmpty()) {
            sPackageNameToUse = null;
        } else if (packagesSupportingCustomTabs.size() == 1) {
            sPackageNameToUse = packagesSupportingCustomTabs.get(0);
        } else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
                && !hasSpecializedHandlerIntents(context, activityIntent)
                && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)) {
            sPackageNameToUse = defaultViewHandlerPackageName;
        } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
            sPackageNameToUse = STABLE_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
            sPackageNameToUse = BETA_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
            sPackageNameToUse = DEV_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
            sPackageNameToUse = LOCAL_PACKAGE;
        }
        return sPackageNameToUse;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static List<String> getCustomTabSupportingPackages(Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            if (isPackageSupportCustomTabs(context, info.activityInfo.packageName)) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }
        return packagesSupportingCustomTabs;
    }

    public static boolean isPackageSupportCustomTabs(Context context, String packageName) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
        serviceIntent.setPackage(packageName);
        return pm.resolveService(serviceIntent, 0) != null;
    }

    /**
     * Used to check whether there is a specialized handler for a given intent.
     *
     * @param intent The intent to check with.
     * @return Whether there is a specialized handler for the given intent.
     */
    private static boolean hasSpecializedHandlerIntents(Context context, Intent intent) {
        try {
            PackageManager pm = context.getApplicationContext().getPackageManager();
            List<ResolveInfo> handlers = pm.queryIntentActivities(
                    intent,
                    PackageManager.GET_RESOLVED_FILTER);
            if (handlers == null || handlers.size() == 0) {
                return false;
            }
            for (ResolveInfo resolveInfo : handlers) {
                IntentFilter filter = resolveInfo.filter;
                if (filter == null) continue;
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue;
                if (resolveInfo.activityInfo == null) continue;
                return true;
            }
        } catch (RuntimeException e) {
            Timber.e("Runtime exception while getting specialized handlers");
        }
        return false;
    }

    /**
     * @return All possible chrome package names that provide custom tabs feature.
     */
    @SuppressWarnings("unused")
    public static String[] getPackages() {
        return new String[]{"", STABLE_PACKAGE, BETA_PACKAGE, DEV_PACKAGE, LOCAL_PACKAGE};
    }
}
