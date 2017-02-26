package arun.com.chromer.shared;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import arun.com.chromer.util.Utils;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.EXTRA_KEY_CLEAR_LAST_TOP_APP;

public class AppDetectService extends Service {
    // Gap at which we polling the system for current foreground app.
    private static final int POLLING_INTERVAL = 400;
    // Needed to turn off polling when screen is turned off.
    private static BroadcastReceiver screenStateReceiver;
    // Flag to control polling.
    private boolean stopPolling = false;
    // Detector to get current foreground app.
    AppDetector appDetector = () -> "";
    // Handler to run our polling.
    private final Handler detectorHandler = new Handler();
    // The runnable which runs out detector.
    private final Runnable appDetectorRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                final String packageName = appDetector.getForegroundPackage();
                AppDetectionManager.getInstance(AppDetectService.this).logPackage(packageName);
            } catch (Exception e) {
                Timber.e(e.toString());
            }
            if (!stopPolling) {
                detectorHandler.postDelayed(this, POLLING_INTERVAL);
            }
        }
    };

    private void clearLastAppIfNeeded(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_KEY_CLEAR_LAST_TOP_APP, false)) {
            AppDetectionManager.getInstance(this).clear();
            Timber.d("Last app cleared");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Utils.canReadUsageStats(this)) {
            Timber.e("Attempted to poll without usage permission");
            stopSelf();
        }
        registerScreenReceiver();
        if (Utils.isLollipopAbove()) {
            appDetector = new LollipopDetector();
        } else {
            appDetector = new PreLollipopDetector();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        clearLastAppIfNeeded(intent);
        startDetection();
        Timber.d("Started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopDetection();
        unregisterReceiver(screenStateReceiver);
        AppDetectionManager.getInstance(this).clear();
        Timber.d("Destroying");
        super.onDestroy();
    }

    private void startDetection() {
        stopPolling = false;
        kickStartDetection();
    }

    private void kickStartDetection() {
        Timber.d("Kick starting polling");
        detectorHandler.post(appDetectorRunnable);
    }

    private void stopDetection() {
        stopPolling = true;
    }

    private void registerScreenReceiver() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        screenStateReceiver = new ScreenStateReceiver();
        registerReceiver(screenStateReceiver, filter);
    }

    @SuppressWarnings("unused")
    void toast(final String toast) {
        new Handler(Looper.getMainLooper())
                .post(() -> Toast.makeText(AppDetectService.this, toast, Toast.LENGTH_SHORT).show());
    }

    private interface AppDetector {
        @NonNull
        String getForegroundPackage();
    }

    private class ScreenStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                stopDetection();
                Timber.d("Turned off polling");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                startDetection();
                Timber.d("Turned on polling");
            }
        }
    }

    private class PreLollipopDetector implements AppDetector {

        @NonNull
        @Override
        public String getForegroundPackage() {
            final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            //noinspection deprecation
            final ActivityManager.RunningTaskInfo runningTaskInfo = am.getRunningTasks(1).get(0);
            if (runningTaskInfo != null) {
                return runningTaskInfo.topActivity.getPackageName();
            }
            return "";
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private class LollipopDetector implements AppDetector {

        @NonNull
        @Override
        public String getForegroundPackage() {
            final long time = System.currentTimeMillis();
            final UsageStatsManager usageMan = (UsageStatsManager) getSystemService(USAGE_STATS_SERVICE);
            final List<UsageStats> stats = usageMan.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000, time);

            final SortedMap<Long, UsageStats> sortedMap = new TreeMap<>();
            for (UsageStats usageStats : stats) {
                if (usageStats != null) {
                    sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
            }
            if (!sortedMap.isEmpty()) {
                final UsageStats usageStats = sortedMap.get(sortedMap.lastKey());
                return usageStats.getPackageName();
            }
            sortedMap.clear();
            return "";
        }
    }
}
