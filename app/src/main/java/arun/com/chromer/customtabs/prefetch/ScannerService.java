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

package arun.com.chromer.customtabs.prefetch;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.customtabs.CustomTabManager;
import arun.com.chromer.customtabs.warmup.WarmUpService;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class ScannerService extends AccessibilityService implements CustomTabManager.ConnectionCallback {

    private static final String SCANNER_SERVICE_NOTIFICATION = "SCANNER_SERVICE_NOTIFICATION";
    private static final String NOTIFICATION_TITLE = "Chromer Scanning Service";
    private static final int NOTIFICATION_ID = 10001;
    private static final int MAX_URL = 3;
    private static final int URL_PREDICTION_DEPTH = 3;
    private static ScannerService sInstance = null;
    private final Stack<AccessibilityNodeInfo> mTreeTraversingStack = new Stack<>();
    private final Queue<String> mExtractedUrlQueue = new LinkedList<>();
    private final LinkedList<CharSequence> mLastTopTexts = new LinkedList<>();
    private final LinkedList<CharSequence> mLocalTopTexts = new LinkedList<>();
    private final List<String> mBrowserList = new LinkedList<>();
    private CustomTabManager mCustomTabManager;
    private String mLastFetchedUrl = "";
    private String mLastPriorityUrl;
    private boolean mShouldStopExtraction = false;

    public static ScannerService getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mCustomTabManager != null) {
            Timber.d("Severing existing connection");
            mCustomTabManager.unbindCustomTabsService(this);
        }

        mCustomTabManager = new CustomTabManager();
        mCustomTabManager.setConnectionCallback(this);

        boolean success = mCustomTabManager.bindCustomTabsService(this);
        Timber.d("Was bound %b", success);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sInstance = null;

        if (mCustomTabManager != null)
            mCustomTabManager.unbindCustomTabsService(this);

        return super.onUnbind(intent);
    }

    @Nullable
    public CustomTabsSession getTabSession() {
        if (mCustomTabManager != null) {
            return mCustomTabManager.getSession();
        }
        return null;
    }

    private void updateNotification() {
        if (mLastFetchedUrl != null && mLastFetchedUrl.length() > 0 && Preferences.get(this).preFetchNotification()) {
            Timber.d("Posting notification");
            PendingIntent contentIntent = PendingIntent.getBroadcast(this,
                    0,
                    new Intent(SCANNER_SERVICE_NOTIFICATION),
                    PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(NOTIFICATION_TITLE);
            inboxStyle.addLine(getString(R.string.last_fetched_urls));
            for (String url : mExtractedUrlQueue) {
                inboxStyle.addLine(url);
            }

            Notification notification = new NotificationCompat.Builder(this)
                    //  .setSmallIcon(R.drawable.ic_chromer_notification)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setContentText(mLastFetchedUrl)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(false)
                    .setStyle(inboxStyle)
                    .setLocalOnly(true)
                    .build();

            NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notifyMgr.notify(NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        sInstance = this;
        mShouldStopExtraction = false;

        // Clear extraction helper stacks before starting new extractions
        clearHolders();

        if (shouldIgnoreEvent(event)) return;

        try {
            stopWarmUpService();

            AccessibilityNodeInfo activeWindowRoot = getRootInActiveWindow();

            // Traverse the tree and act on every text
            mTreeTraversingStack.push(activeWindowRoot);
            while (!mTreeTraversingStack.empty() && mExtractedUrlQueue.size() < MAX_URL && !mShouldStopExtraction) {
                AccessibilityNodeInfo currNode = mTreeTraversingStack.pop();
                if (currNode != null) {
                    processNode(currNode);
                    for (int i = 0; i < currNode.getChildCount() && !mShouldStopExtraction; i++) {
                        mTreeTraversingStack.push(currNode.getChild(i));
                    }
                }
            }

            mTreeTraversingStack.clear();

            // Don't need the root node anymore, recycle it.
            if (activeWindowRoot != null) activeWindowRoot.recycle();

            if (mExtractedUrlQueue.size() > 0 && !mShouldStopExtraction) {
                mLastPriorityUrl = mExtractedUrlQueue.poll();

                Timber.d("Priority : %s", mLastPriorityUrl);

                List<Bundle> possibleUrls = new ArrayList<>();
                for (String url : mExtractedUrlQueue) {
                    if (url == null) continue;

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(CustomTabsService.KEY_URL, Uri.parse(url));
                    possibleUrls.add(bundle);
                    Timber.d("Others : %s", url);
                }

                boolean success;
                if (mLastPriorityUrl != null) {
                    if (!mLastPriorityUrl.equalsIgnoreCase(mLastFetchedUrl)) {
                        success = mCustomTabManager.mayLaunchUrl(Uri.parse(mLastPriorityUrl), null, possibleUrls);
                        if (success) mLastFetchedUrl = mLastPriorityUrl;
                    } else {
                        Timber.d("Ignored, already fetched");
                    }
                }
                updateNotification();
            }
            mExtractedUrlQueue.clear();
        } catch (Exception e) {
            clearHolders();
            e.printStackTrace();
        }
    }

    private void clearHolders() {
        mTreeTraversingStack.clear();
        mExtractedUrlQueue.clear();
        mLocalTopTexts.clear();
    }

    private boolean shouldIgnoreEvent(AccessibilityEvent event) {
        if (!Preferences.get(this).preFetch()) return true;

        if (!isWifiConditionsMet()) return true;

        String packageName;
        if (event.getPackageName() != null) {
            packageName = event.getPackageName().toString();
            return packageName.equalsIgnoreCase(Preferences.get(this).customTabApp())
                    || getBrowserPackageList().contains(packageName);
        }
        return false;
    }

    private List<String> getBrowserPackageList() {
        if (mBrowserList.isEmpty()) {
            Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
            @SuppressLint("InlinedApi") List<ResolveInfo> resolvedActivityList = getApplicationContext()
                    .getPackageManager().queryIntentActivities(activityIntent, PackageManager.MATCH_ALL);
            for (ResolveInfo info : resolvedActivityList) {
                if (info != null) {
                    mBrowserList.add(info.activityInfo.packageName);
                }
            }
        }
        return mBrowserList;
    }

    private void processNode(@NonNull AccessibilityNodeInfo node) {
        if (node.getText() != null) {
            String text = node.getText().toString();

            if (mLocalTopTexts.size() < URL_PREDICTION_DEPTH) mLocalTopTexts.add(node.getText());
            if (mLastTopTexts.size() < URL_PREDICTION_DEPTH) mLastTopTexts.add(node.getText());

            if (mLastTopTexts.equals(mLocalTopTexts) && mLastTopTexts.size() == URL_PREDICTION_DEPTH) {
                Timber.d("Predicted no url in current screen, stopping.");
                mShouldStopExtraction = true;
                return;
            } else if (mLocalTopTexts.size() == URL_PREDICTION_DEPTH) {
                mLastTopTexts.clear();
            }

            if (text.equalsIgnoreCase(NOTIFICATION_TITLE)) {
                Timber.d("Ignoring extraction from our own notification");
                mShouldStopExtraction = true;
            } else {
                extractURL(text);
            }
        }
    }


    private void extractURL(@NonNull String string) {
        Matcher m = Pattern.compile("\\b((?:[a-z][\\w-]+:(?:/{1,3}|[a-z0-9%])|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
                .matcher(string);
        while (m.find()) {
            String url = m.group();
            if (!url.toLowerCase().matches("^\\w+://.*")) {
                url = "http://" + url;
            }

            mLastTopTexts.clear();
            mExtractedUrlQueue.add(url);

            if (mExtractedUrlQueue.size() == 1 && url.equalsIgnoreCase(mLastPriorityUrl)) {
                // This means the new extraction is giving the same urls as last extraction did.
                // In this case, we will explicitly stop the tree traversal by setting a flag variable.
                Timber.d("Encountered same priority url twice, stopping extraction");
                mShouldStopExtraction = true;
            }
        }
    }


    private void stopWarmUpService() {
        try {
            stopService(new Intent(this, WarmUpService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isWifiConditionsMet() {
        if (Preferences.get(this).wifiOnlyPrefetch()) {
            final WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiMgr.isWifiEnabled()) {
                final WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                return !(wifiInfo != null && wifiInfo.getNetworkId() == -1);
            } else {
                return false;
            }
        } else
            return true;
    }

    @Override
    public void onInterrupt() {
        // Nothing
    }

    @Override
    public void onCustomTabsConnected() {

    }

    @Override
    public void onCustomTabsDisconnected() {

    }

    @Override
    public void onDestroy() {
        if (mCustomTabManager != null)
            mCustomTabManager.unbindCustomTabsService(this);
        clearHolders();
        super.onDestroy();
    }
}
