package arun.com.chromer.chrometabutilites;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;

import org.chromium.customtabsclient.ServiceConnection;
import org.chromium.customtabsclient.ServiceConnectionCallback;

import java.util.List;

/**
 * Created by Arun on 18/12/2015.
 */
public class MyCustomActivityHelper implements ServiceConnectionCallback {

    private static String TAG = MyCustomActivityHelper.class.getSimpleName();

    private CustomTabsSession mCustomTabsSession;
    private CustomTabsClient mClient;
    private CustomTabsServiceConnection mConnection;
    private ConnectionCallback mConnectionCallback;

    /**
     * Opens the URL on a Custom Tab if possible. Otherwise fallsback to opening it on a WebView.
     *
     * @param activity         The host activity.
     * @param customTabsIntent a CustomTabsIntent to be used if Custom Tabs is available.
     * @param uri              the Uri to be opened.
     * @param fallback         a CustomTabsFallback to be used if Custom Tabs is not available.
     */
    public static void openCustomTab(Activity activity,
                                     CustomTabsIntent customTabsIntent,
                                     Uri uri,
                                     CustomTabsFallback fallback) {

        // getting all the packages here
        String packageName = MyCustomTabHelper.getPackageNameToUse(activity);

        //If we cant find a package name, it means theres no browser that supports
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
                Log.d(TAG, "Called fallback even though package was found, weird");
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
     * @param activity the activity that is connected to the service.
     */
    public void unbindCustomTabsService(Activity activity) {
        Log.d(TAG, "Attempting to unbind service!");
        if (mConnection == null) return;
        activity.unbindService(mConnection);
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
    public CustomTabsSession getSession() {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient.newSession(null);
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
     * Binds the Activity to the Custom Tabs Service.
     *
     * @param activity the activity to be binded to the service.
     */
    public void bindCustomTabsService(Activity activity) {
        Log.d(TAG, "Attempting to bind custom tabs service");
        if (mClient != null) return;

        String packageName = MyCustomTabHelper.getPackageNameToUse(activity);
        if (packageName == null) return;

        mConnection = new ServiceConnection(this);
        boolean ok = CustomTabsClient.bindCustomTabsService(activity, packageName, mConnection);
        if (ok) {
            Log.d(TAG, "Bound successfully");
        } else
            Log.d(TAG, "Did not bind, something wrong");
    }

    public boolean mayLaunchUrl(Uri uri, Bundle extras, List<Bundle> otherLikelyBundles) {
        Log.d(TAG, "Attempting may launch url");
        if (mClient == null) return false;

        CustomTabsSession session = getSession();
        if (session == null) return false;

        boolean ok = session.mayLaunchUrl(uri, extras, otherLikelyBundles);
        if (ok) {
            Log.d(TAG, "Successfully warmed up with may launch URL");
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
}
