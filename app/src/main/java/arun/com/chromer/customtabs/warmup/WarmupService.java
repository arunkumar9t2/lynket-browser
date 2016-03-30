package arun.com.chromer.customtabs.warmup;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsSession;

import java.util.List;

import arun.com.chromer.customtabs.CustomActivityHelper;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class WarmupService extends Service implements CustomActivityHelper.ConnectionCallback {

    private static WarmupService mWarmupService = null;

    private CustomActivityHelper mCustomActivityHelper;

    public static WarmupService getInstance() {
        return mWarmupService;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mCustomActivityHelper != null) {
            // Already an instance exists, so we will un bind the current connection and then bind again.
            Timber.d("Severing existing connection");
            mCustomActivityHelper.unbindCustomTabsService(this);
        }


        mCustomActivityHelper = new CustomActivityHelper();
        mCustomActivityHelper.setConnectionCallback(this);
        boolean success = mCustomActivityHelper.bindCustomTabsService(this);
        Timber.d("Was binded %b", success);
        mWarmupService = this;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mCustomActivityHelper != null)
            mCustomActivityHelper.unbindCustomTabsService(this);
        mCustomActivityHelper = null;
        mWarmupService = null;
        Timber.d("Died");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mWarmupService = null;
        if (mCustomActivityHelper != null)
            mCustomActivityHelper.unbindCustomTabsService(this);

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
        boolean ok = mCustomActivityHelper.mayLaunchUrl(uri, null, possibleUrls);
        Timber.d("Warmup %b", ok);
        return ok;
    }

    public CustomTabsSession getTabSession() {
        if (mCustomActivityHelper != null) {
            return mCustomActivityHelper.getSession();
        }
        return null;
    }
}
