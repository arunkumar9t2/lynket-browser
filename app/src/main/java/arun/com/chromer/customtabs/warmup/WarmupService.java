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
public class WarmupService extends Service implements CustomTabBindingHelper.ConnectionCallback {

    private static WarmupService sWarmupService = null;

    public static WarmupService getInstance() {
        return sWarmupService;
    }

    private CustomTabBindingHelper mCustomTabBindingHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sWarmupService = this;
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
        sWarmupService = null;
        Timber.d("Died");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sWarmupService = null;
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
