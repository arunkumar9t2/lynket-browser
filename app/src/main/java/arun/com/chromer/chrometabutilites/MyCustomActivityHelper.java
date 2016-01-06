package arun.com.chromer.chrometabutilites;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;

import java.util.List;

import arun.com.chromer.util.Util;

/**
 * Created by Arun on 18/12/2015.
 */
public class MyCustomActivityHelper implements ServiceConnectionCallback {

    private static final String TAG = MyCustomActivityHelper.class.getSimpleName();

    private CustomTabsSession mCustomTabsSession;
    private CustomTabsClient mClient;
    private CustomTabsServiceConnection mConnection;
    private ConnectionCallback mConnectionCallback;
    private NavigationCallback mNavigationCallback;

    /**
     * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
     *
     * @param activity         The host activity.
     * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
     * @param uri              the Uri to be opened.
     * @param fallback         a CustomTabsFallback to be used if Custom Tabs is not available.
     */
    @SuppressWarnings("SameParameterValue")
    public static void openCustomTab(Activity activity,
                                     CustomTabsIntent customTabsIntent,
                                     Uri uri,
                                     CustomTabsFallback fallback) {
        // The package name to use
        String packageName;

        // first check user preferred custom provider is there
        String userPrefProvider = activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE)
                .getString("preferred_package", null);
        if (userPrefProvider != null && Util.isPackageInstalled(activity, userPrefProvider)) {
            // TODO optionally check if preferred selection is valid by comparing with all providers
            Log.d(TAG, "Valid user preferred custom tab provider present");
            packageName = userPrefProvider;
        } else {
            // getting all the packages here
            Log.d(TAG, "Valid user choice not present, defaulting to conventional method");
            packageName = MyCustomTabHelper.getPackageNameToUse(activity);
        }
        //If we cant find a package name, it means there's no browser that supports
        //Chrome Custom Tabs installed. So, we fallback to the webview
        if (packageName == null) {
            Log.d(TAG, "Called fallback since no package found!");
            callFallback(activity, uri, fallback);
        } else {
            customTabsIntent.intent.setPackage(packageName);
            try {
                customTabsIntent.launchUrl(activity, uri);
                Log.d(TAG, "Launched url:" + uri.toString());
            } catch (Exception e) {
                callFallback(activity, uri, fallback);
                Log.d(TAG, "Called fallback even though package was found, weird Exception :" + e.toString());
            }
        }
    }

    private static void callFallback(Activity activity, Uri uri, CustomTabsFallback fallback) {
        if (fallback != null) {
            fallback.openUri(activity, uri);
        }
    }

    /**
     * Unbinds the Activity from the Custom Tabs Service.
     *
     * @param context the activity that is connected to the service.
     */
    public void unbindCustomTabsService(Context context) {
        Log.d(TAG, "Attempting to unbind service!");
        if (mConnection == null) return;
        context.unbindService(mConnection);
        mClient = null;
        mCustomTabsSession = null;
        mConnection = null;
        Log.d(TAG, "Unbounded service!");
    }

    /**
     * Creates or retrieves an exiting CustomTabsSession.
     *
     * @return a CustomTabsSession.
     */
    @SuppressWarnings("WeakerAccess")
    public CustomTabsSession getSession() {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient.newSession(mNavigationCallback);
        }
        return mCustomTabsSession;
    }

    /**
     * Register a Callback to be called when connected or disconnected from the Custom Tabs Service.
     *
     * @param connectionCallback
     */
    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.mConnectionCallback = connectionCallback;
    }

    /**
     * Register a Callback to be called when Navigation occurs..
     *
     * @param navigationCallback
     */
    public void setNavigationCallback(NavigationCallback navigationCallback) {
        this.mNavigationCallback = navigationCallback;
    }

    /**
     * Binds the Activity to the Custom Tabs Service.
     *
     * @param context the activity to be binded to the service.
     */
    public boolean bindCustomTabsService(Context context) {
        Log.d(TAG, "Attempting to bind custom tabs service");
        if (mClient != null) return false;

        String packageName = MyCustomTabHelper.getPackageNameToUse(context);
        if (packageName == null) return false;

        mConnection = new ServiceConnection(this);
        boolean ok = CustomTabsClient.bindCustomTabsService(context, packageName, mConnection);
        if (ok) {
            Log.d(TAG, "Bound successfully");
        } else
            Log.d(TAG, "Did not bind, something wrong");
        return ok;
    }

    @SuppressWarnings("SameParameterValue")
    public boolean mayLaunchUrl(Uri uri, Bundle extras, List<Bundle> otherLikelyBundles) {
        Log.d(TAG, "Attempting may launch url");
        if (mClient == null) return false;

        CustomTabsSession session = getSession();
        if (session == null) return false;

        boolean ok = session.mayLaunchUrl(uri, extras, otherLikelyBundles);
        if (ok) {
            Log.d(TAG, "Successfully warmed up with may launch URL:" + uri.toString());
        } else {
            Log.d(TAG, "May launch url was a failure for " + uri.toString());
        }
        return ok;
    }

    @Override
    public void onServiceConnected(CustomTabsClient client) {
        Log.d(TAG, "Service connected properly!");
        mClient = client;
        mClient.warmup(0L);
        if (mConnectionCallback != null) mConnectionCallback.onCustomTabsConnected();
    }


    public boolean requestWarmUp() {
        boolean x = false;
        if (mClient != null) {
            x = mClient.warmup(0L);
            Log.d(TAG, "Warmup status " + x);
        }
        return false;
    }

    @Override
    public void onServiceDisconnected() {
        Log.d(TAG, "Service disconnected!");
        mClient = null;
        mCustomTabsSession = null;
        if (mConnectionCallback != null) mConnectionCallback.onCustomTabsDisconnected();
    }

    /**
     * A Callback for when the service is connected or disconnected. Use those callbacks to
     * handle UI changes when the service is connected or disconnected.
     */
    public interface ConnectionCallback {
        /**
         * Called when the service is connected.
         */
        void onCustomTabsConnected();

        /**
         * Called when the service is disconnected.
         */
        void onCustomTabsDisconnected();
    }

    /**
     * To be used as a fallback to open the Uri when Custom Tabs is not available.
     */
    public interface CustomTabsFallback {
        /**
         * @param activity The Activity that wants to open the Uri.
         * @param uri      The uri to be opened by the fallback.
         */
        void openUri(Activity activity, Uri uri);
    }

    private static class NavigationCallback extends CustomTabsCallback {
        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
            Log.w(TAG, "onNavigationEvent: Code = " + navigationEvent);
        }
    }
}
