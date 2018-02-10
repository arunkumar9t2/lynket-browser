/*
 * Chromer
 * Copyright (C) 2017 Arunkumar
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package arun.com.chromer.appdetect;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Inject;

import arun.com.chromer.R;
import arun.com.chromer.di.service.ServiceComponent;
import arun.com.chromer.shared.base.service.BaseService;
import arun.com.chromer.util.Utils;
import timber.log.Timber;

import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_CLEAR_LAST_TOP_APP;

public class AppDetectService extends BaseService {
    // Gap at which we polling the system for current foreground app.
    private static final int POLLING_INTERVAL = 400;

    private static final String CHANNEL_ID = "App detection service";

    // Needed to turn off polling when screen is turned off.
    private BroadcastReceiver screenStateReceiver;

    // Flag to control polling.
    private boolean stopPolling = false;
    // Detector to get current foreground app.
    private AppDetector appDetector = () -> "";
    // Handler to run our polling.
    private final Handler detectorHandler = new Handler();
    // The runnable which runs out detector.
    private final Runnable appDetectorRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                final String packageName = appDetector.getForegroundPackage();
                appDetectionManager.logPackage(packageName);
            } catch (Exception e) {
                Timber.e(e.toString());
            }
            if (!stopPolling) {
                detectorHandler.postDelayed(this, POLLING_INTERVAL);
            }
        }
    };

    @Inject
    AppDetectionManager appDetectionManager;

    private void clearLastAppIfNeeded(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_KEY_CLEAR_LAST_TOP_APP, false)) {
            appDetectionManager.clear();
            Timber.d("Last app cleared");
        }
    }

    @Override
    public void onCreate() {
        initChannels();
        super.onCreate();

        if (Utils.ANDROID_OREO) {
            startForeground(1, new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_chromer_notification)
                    .setPriority(PRIORITY_MIN)
                    .setContentText(getString(R.string.app_detection_service_explanation))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.app_detection_service_explanation))
                            .setBigContentTitle(getString(R.string.app_detection_service)))
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setContentTitle(getString(R.string.app_detection_service))
                    .setAutoCancel(false)
                    .setLocalOnly(true)
                    .build());
        }

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

    private void initChannels() {
        if (Utils.ANDROID_OREO) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "App Detection Service", NotificationManager.IMPORTANCE_MIN);
            channel.setDescription(getString(R.string.app_detection_notification_channel_description));
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void inject(ServiceComponent serviceComponent) {
        serviceComponent.inject(this);
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
        try {
            unregisterReceiver(screenStateReceiver);
        } catch (IllegalStateException e) {
            Timber.e(e);
        }
        appDetectionManager.clear();
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
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                stopDetection();
                Timber.d("Turned off polling");
            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
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
            final List<UsageStats> stats = usageMan.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 3, time);

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
