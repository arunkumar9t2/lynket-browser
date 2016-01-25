package arun.com.chromer;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import timber.log.Timber;

public class AppDetectService extends Service {

    private static final int POLLING_INTERVAL = 350;
    private static AppDetectService mAppDetectService = null;
    private static Thread mPollThread;

    private static BroadcastReceiver mScreenReceiver;

    private static String mLastDetectedApp = "";

    private boolean mShouldStopPolling = false;
    private Runnable mAppDetectRunnable = new Runnable() {
        @Override
        public void run() {
            // Timber.d("Detection thread started");
            while (!mShouldStopPolling) {
                try {
                    String packageName = "";

                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    ActivityManager.RunningTaskInfo runningTaskInfo = am.getRunningTasks(1).get(0);
                    if (runningTaskInfo != null) {
                        packageName = runningTaskInfo.topActivity.getPackageName();
                    }

                    if (!mLastDetectedApp.equalsIgnoreCase(packageName)) {
                        mLastDetectedApp = packageName;
                        Timber.d("Current app " + packageName);
                    }

                    // Sleep and continue again.
                    Thread.sleep(POLLING_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Timber.d("Detection thread stopped");
        }
    };

    public AppDetectService() {
    }

    public static AppDetectService getInstance() {
        return mAppDetectService;
    }

    public String getLastApp() {
        return mLastDetectedApp.trim();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("Started");
        mAppDetectService = this;
        registerScreenReceiver();
        startDetection();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Timber.d("Destroying");
        unregisterReceiver(mScreenReceiver);
        // TODO Potential leak??
        mAppDetectService = null;
        super.onDestroy();
    }

    private void startDetection() {
        mPollThread = null;
        // Create a new instance to start thread again
        mPollThread = new Thread(mAppDetectRunnable);
        mPollThread.start();
    }

    private void registerScreenReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenReceiver = new ScreenReceiver();
        registerReceiver(mScreenReceiver, filter);
    }

    void toast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                mShouldStopPolling = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                mShouldStopPolling = false;
                startDetection();
            }
        }
    }
}
