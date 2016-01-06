package arun.com.chromer.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;

import arun.com.chromer.chrometabutilites.MyCustomActivityHelper;

/**
 * Created by Arun on 06/01/2016.
 */
public class WarmupService extends Service implements MyCustomActivityHelper.ConnectionCallback {

    private static final String TAG = WarmupService.class.getSimpleName();

    private static WarmupService mWarmupService = null;

    MyCustomActivityHelper myCustomActivityHelper;

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
        Log.d(TAG, "Was binded " + success);
        mWarmupService = this;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myCustomActivityHelper != null)
            myCustomActivityHelper.unbindCustomTabsService(this);
        myCustomActivityHelper = null;
        mWarmupService = null;
        Log.d(TAG, "Died");
    }

    @Override
    public void onCustomTabsConnected() {
        Log.d(TAG, "Connected to custom tabs");
    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    public CustomTabsSession getTabSession() {
        if (myCustomActivityHelper != null) {
            return myCustomActivityHelper.getSession();
        }
        return null;
    }
}
