package arun.com.chromer.services;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import arun.com.chromer.preferences.Preferences;
import arun.com.chromer.util.Constants;
import timber.log.Timber;

public class AppDetectService extends Service {

    private static final int POLLING_INTERVAL = 350;

    private static AppDetectService sAppDetectService = null;

    private static BroadcastReceiver mScreenStateReceiver;

    private static String mLastDetectedApp = "";

    private boolean mShouldStopPolling = false;

    private final Runnable mAppDetectRunnable = new Runnable() {

        @SuppressWarnings("deprecation")
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            // Timber.d("Detection thread started");
            while (!mShouldStopPolling) {
                try {
                    String packageName = getCurrentForegroundApp();

                    if (!mLastDetectedApp.equalsIgnoreCase(packageName) && isAllowedPackage(packageName)) {
                        mLastDetectedApp = packageName;
                        Timber.d("Current app %s", packageName);
                    }

                    // Sleep and continue again.
                    Thread.sleep(POLLING_INTERVAL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Timber.d("Detection thread stopped");
        }
    };

    private String getCurrentForegroundApp() {
        String packageName = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Lollipop above
            long time = System.currentTimeMillis();

            UsageStatsManager usageMan = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            List<UsageStats> stats = usageMan.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);

            SortedMap<Long, UsageStats> sortedMap = new TreeMap<>();
            for (UsageStats usageStats : stats) {
                // Store the list in a sorted map, will be used to retrieve the recent app later
                if (usageStats != null)
                    sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }

            if (!sortedMap.isEmpty()) {
                final UsageStats usageStats = sortedMap.get(sortedMap.lastKey());
                packageName = usageStats.getPackageName();
            }
        } else {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            //noinspection deprecation
            ActivityManager.RunningTaskInfo runningTaskInfo = am.getRunningTasks(1).get(0);
            if (runningTaskInfo != null) {
                packageName = runningTaskInfo.topActivity.getPackageName();
            }
        }
        return packageName;
    }

    public AppDetectService() {
    }

    public static AppDetectService getInstance() {
        return sAppDetectService;
    }

    @NonNull
    public String getLastApp() {
        if (mLastDetectedApp != null) {
            return mLastDetectedApp.trim();
        } else return "";
    }

    private void clearLastAppIfNeeded(Intent intent) {
        if (intent != null && intent.getBooleanExtra(Constants.EXTRA_KEY_CLEAR_LAST_TOP_APP, false)) {
            mLastDetectedApp = "";
            Timber.d("Last app cleared");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Timber.d("Started");
        sAppDetectService = this;

        registerScreenReceiver();

        clearLastAppIfNeeded(intent);

        startDetection();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        mShouldStopPolling = true;

        Timber.d("Destroying");
        unregisterReceiver(mScreenStateReceiver);
        sAppDetectService = null;
        super.onDestroy();
    }

    private void startDetection() {
        Thread mPollThread;
        // Create a new instance to start thread again
        mPollThread = new Thread(mAppDetectRunnable);
        mPollThread.start();
    }

    private void registerScreenReceiver() {
        // We should prevent the same receiver from registering again, to do this we will attempt to
        // register a existing instance of Screen receiver and check if IllegalArgumentException
        // is thrown.
        if (mScreenStateReceiver != null) {
            try {
                unregisterReceiver(mScreenStateReceiver);
            } catch (Exception ignored) {
            }
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenStateReceiver = new ScreenStateReceiver();
        registerReceiver(mScreenStateReceiver, filter);
    }

    @SuppressWarnings("unused")
    void toast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    private boolean isAllowedPackage(String packageName) {
        // Ignore system pop ups
        if (packageName.equalsIgnoreCase("android")) return false;

        // Ignore our app
        if (packageName.equalsIgnoreCase(getPackageName())) return false;

        // Chances are that we picked the opening custom tab, so let's ignore our default provider
        // to be safe
        if (packageName.equalsIgnoreCase(Preferences.customTabApp(this))) return false;

        // Ignore google quick search box
        if (packageName.equalsIgnoreCase("com.google.android.googlequicksearchbox")) return false;

        // There can also be cases where there is no default provider set, so lets ignore all possible
        // custom tab providers to be sure. This is safe since browsers don't call our app anyways.

        // Commenting, research needed
        // if (mCustomTabPackages.contains(packageName)) return true;

        return true;
    }

    public class ScreenStateReceiver extends BroadcastReceiver {

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
