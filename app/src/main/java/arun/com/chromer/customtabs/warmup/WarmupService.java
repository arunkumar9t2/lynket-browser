package arun.com.chromer.customtabs.warmup;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsSession;

import java.util.List;

import arun.com.chromer.customtabs.CustomTabBindingHelper;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class WarmUpService extends Service implements CustomTabBindingHelper.ConnectionCallback {

    private static WarmUpService sWarmUpService = null;
    private CustomTabBindingHelper mCustomTabBindingHelper;

    public static WarmUpService getInstance() {
        return sWarmUpService;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sWarmUpService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mCustomTabBindingHelper != null) {
            // Already an instance exists, so we will un bind the current connection and then bind again.
            Timber.d("Severing existing connection");
            mCustomTabBindingHelper.unbindCustomTabsService(this);
        }


        mCustomTabBindingHelper = new CustomTabBindingHelper();
        mCustomTabBindingHelper.setConnectionCallback(this);
        boolean success = mCustomTabBindingHelper.bindCustomTabsService(this);
        Timber.d("Was bound %b", success);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mCustomTabBindingHelper != null)
            mCustomTabBindingHelper.unbindCustomTabsService(this);
        mCustomTabBindingHelper = null;
        sWarmUpService = null;
        Timber.d("Died");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sWarmUpService = null;
        if (mCustomTabBindingHelper != null)
            mCustomTabBindingHelper.unbindCustomTabsService(this);

        return super.onUnbind(intent);
    }

    @Override
    public void onCustomTabsConnected() {
        Timber.d("Connected to custom tabs");
    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    public boolean mayLaunchUrl(Uri uri, List<Bundle> possibleUrls) {
        boolean ok = mCustomTabBindingHelper.mayLaunchUrl(uri, null, possibleUrls);
        Timber.d("Warmup %b", ok);
        return ok;
    }

    public CustomTabsSession getTabSession() {
        if (mCustomTabBindingHelper != null) {
            return mCustomTabBindingHelper.getSession();
        }
        return null;
    }
}
