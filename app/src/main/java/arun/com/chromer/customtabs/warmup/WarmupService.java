package arun.com.chromer.customtabs.warmup;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsSession;

import arun.com.chromer.customtabs.CustomTabManager;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class WarmUpService extends Service implements CustomTabManager.ConnectionCallback {

    private static WarmUpService sWarmUpService = null;
    private CustomTabManager mCustomTabManager;

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
        if (mCustomTabManager != null) {
            // Already an instance exists, so we will un bind the current connection and then bind again.
            Timber.d("Severing existing connection");
            mCustomTabManager.unbindCustomTabsService(this);
        }
        mCustomTabManager = new CustomTabManager();
        mCustomTabManager.setConnectionCallback(this);

        boolean success = mCustomTabManager.bindCustomTabsService(this);
        Timber.d("Was bound %b", success);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mCustomTabManager != null)
            mCustomTabManager.unbindCustomTabsService(this);
        mCustomTabManager = null;
        sWarmUpService = null;
        Timber.d("Died");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sWarmUpService = null;
        if (mCustomTabManager != null)
            mCustomTabManager.unbindCustomTabsService(this);
        return super.onUnbind(intent);
    }

    @Override
    public void onCustomTabsConnected() {
        Timber.d("Connected to custom tabs");
    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    public CustomTabsSession getTabSession() {
        if (mCustomTabManager != null) {
            return mCustomTabManager.getSession();
        }
        return null;
    }
}
