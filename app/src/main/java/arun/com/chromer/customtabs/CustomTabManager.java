package arun.com.chromer.customtabs;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

import java.util.List;

import arun.com.chromer.activities.settings.preferences.manager.Preferences;
import timber.log.Timber;

/**
 * Created by Arun on 18/12/2015.
 * Helper class to maintain connection with custom tab provider. Responsible to connection with
 * the service and issuing warm up and other optimizations.
 */
public class CustomTabManager implements ServiceConnectionCallback {
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsClient mClient;
    private CustomTabsServiceConnection mConnection;
    private ConnectionCallback mConnectionCallback;
    private NavigationCallback mNavigationCallback;

    /**
     * Unbinds the component from the Custom Tabs Service.
     *
     * @param context the component that is connected to the service.
     */
    public void unbindCustomTabsService(Context context) {
        if (mConnection == null) return;
        try {
            context.unbindService(mConnection);
        } catch (IllegalArgumentException e) {
            Timber.e("Ignored exception trying to unbind without binding first");
        }
        mClient = null;
        mCustomTabsSession = null;
        mConnection = null;
        Timber.d("Unbounded service!");
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
            mCustomTabsSession = mClient.newSession(mNavigationCallback);
        }
        return mCustomTabsSession;
    }

    /**
     * Register a Callback to be called when connected or disconnected from the Custom Tabs Service.
     */
    public void setConnectionCallback(ConnectionCallback connectionCallback) {
        this.mConnectionCallback = connectionCallback;
    }

    /**
     * Register a Callback to be called when Navigation occurs..
     */
    public void setNavigationCallback(NavigationCallback navigationCallback) {
        this.mNavigationCallback = navigationCallback;
    }

    /**
     * Binds the component to the Custom Tabs Service.
     *
     * @param context the component to be bound to the service.
     */
    public boolean bindCustomTabsService(Context context) {
        if (mClient != null) return false;

        String packageName = Preferences.get(context).customTabApp();
        if (packageName == null) return false;

        mConnection = new ServiceConnection(this);
        boolean ok = CustomTabsClient.bindCustomTabsService(context, packageName, mConnection);
        if (ok) {
            Timber.d("Bound successfully with %s", packageName);
        } else
            Timber.d("Did not bind, something wrong");
        return ok;
    }


    @SuppressWarnings("SameParameterValue")
    public boolean mayLaunchUrl(Uri uri, Bundle extras, List<Bundle> otherLikelyBundles) {
        if (mClient == null) return false;

        CustomTabsSession session = getSession();
        if (session == null) return false;

        boolean ok = session.mayLaunchUrl(uri, extras, otherLikelyBundles);
        if (ok) {
            Timber.d("Successfully warmed up with may launch URL: %s", uri.toString());
        } else {
            Timber.d("May launch url was a failure for %s", uri.toString());
        }
        return ok;
    }

    @Override
    public void onServiceConnected(CustomTabsClient client) {
        mClient = client;
        mClient.warmup(0L);
        if (mConnectionCallback != null) mConnectionCallback.onCustomTabsConnected();
    }


    public boolean requestWarmUp() {
        boolean ok = false;
        if (mClient != null) {
            ok = mClient.warmup(0L);
            Timber.d("Warmup status %s", ok);
        }
        return ok;
    }

    @Override
    public void onServiceDisconnected() {
        Timber.d("Service disconnected!");
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

    public static class NavigationCallback extends CustomTabsCallback {
        @Override
        public void onNavigationEvent(int navigationEvent, Bundle extras) {
        }
    }
}
