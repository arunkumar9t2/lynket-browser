package arun.com.chromer.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsSession;

import java.util.List;

import arun.com.chromer.chrometabutilites.MyCustomActivityHelper;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class WarmupService extends Service implements MyCustomActivityHelper.ConnectionCallback {

    private static final String TAG = WarmupService.class.getSimpleName();

    private static WarmupService mWarmupService = null;

    private MyCustomActivityHelper myCustomActivityHelper;

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
        myCustomActivityHelper = new MyCustomActivityHelper();
        myCustomActivityHelper.setConnectionCallback(this);
        boolean success = myCustomActivityHelper.bindCustomTabsService(this);
        Timber.d("Was binded " + success);
        mWarmupService = this;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (myCustomActivityHelper != null)
            myCustomActivityHelper.unbindCustomTabsService(this);
        myCustomActivityHelper = null;
        mWarmupService = null;
        Timber.d("Died");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mWarmupService = null;
        if (myCustomActivityHelper != null)
            myCustomActivityHelper.unbindCustomTabsService(this);

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
        boolean ok = myCustomActivityHelper.mayLaunchUrl(uri, null, possibleUrls);
        Timber.d("Warmup " + ok);
        return ok;
    }

    public CustomTabsSession getTabSession() {
        if (myCustomActivityHelper != null) {
            return myCustomActivityHelper.getSession();
        }
        return null;
    }
}
