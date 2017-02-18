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

    private static WarmUpService warmUpService = null;
    private CustomTabManager customTabManager;

    public static WarmUpService getInstance() {
        return warmUpService;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        warmUpService = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (customTabManager != null) {
            // Already an instance exists, so we will un bind the current connection and then bind again.
            Timber.d("Severing existing connection");
            customTabManager.unbindCustomTabsService(this);
        }
        customTabManager = new CustomTabManager();
        customTabManager.setConnectionCallback(this);

        boolean success = customTabManager.bindCustomTabsService(this);
        Timber.d("Was bound %b", success);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (customTabManager != null)
            customTabManager.unbindCustomTabsService(this);
        customTabManager = null;
        warmUpService = null;
        Timber.d("Died");
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        warmUpService = null;
        if (customTabManager != null)
            customTabManager.unbindCustomTabsService(this);
        return super.onUnbind(intent);
    }

    @Override
    public void onCustomTabsConnected() {
        Timber.d("Connected to custom tabs");
    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    @Nullable
    public CustomTabsSession getTabSession() {
        if (customTabManager != null) {
            return customTabManager.getSession();
        }
        return null;
    }
}
