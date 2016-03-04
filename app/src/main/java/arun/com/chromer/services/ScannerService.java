package arun.com.chromer.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsService;
import android.support.customtabs.CustomTabsSession;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import arun.com.chromer.chrometabutilites.CustomActivityHelper;
import arun.com.chromer.util.Preferences;
import timber.log.Timber;

/**
 * Created by Arun on 06/01/2016.
 */
public class ScannerService extends AccessibilityService implements CustomActivityHelper.ConnectionCallback {

    private static ScannerService sInstance = null;
    private final int MAX_URL = 3;
    private final Stack<AccessibilityNodeInfo> mTreeTraversingStack = new Stack<>();
    private final Queue<String> mExtractedUrlStack = new LinkedList<>();
    private String mLastFetchedUrl = "";
    private String mLastPriorityUrl;
    private boolean mShouldStopExtraction = false;
    private CustomActivityHelper mCustomActivityHelper;

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
        if (mCustomActivityHelper != null) {
            Timber.d("Severing existing connection");
            mCustomActivityHelper.unbindCustomTabsService(this);
        }
        mCustomActivityHelper = new CustomActivityHelper();
        mCustomActivityHelper.setConnectionCallback(this);
        boolean success = mCustomActivityHelper.bindCustomTabsService(this);
        Timber.d("Was bound %b", success);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        sInstance = null;
        mCustomActivityHelper.unbindCustomTabsService(this);
        Timber.d("Unbinding");
        return super.onUnbind(intent);
    }

    public CustomTabsSession getTabSession() {
        if (mCustomActivityHelper != null) {
            return mCustomActivityHelper.getSession();
        }
        return null;
    }


    public boolean mayLaunchUrl(Uri uri, List<Bundle> possibleUrls) {
        if (!Preferences.preFetch(this)) return false;

        boolean ok = mCustomActivityHelper.mayLaunchUrl(uri, null, possibleUrls);
        Timber.d("Warm up %b", ok);
        return ok;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        sInstance = this;
        mShouldStopExtraction = false;
        // Clear extraction helper stacks before starting new extractions
        emptyStacks();

        if (shouldIgnoreEvent(event)) return;

        try {
            stopWarmUpService();

            AccessibilityNodeInfo activeWindowRoot = getRootInActiveWindow();

            // Traverse the tree and act on every text
            mTreeTraversingStack.push(activeWindowRoot);
            while (!mTreeTraversingStack.empty() && mExtractedUrlStack.size() < MAX_URL && !mShouldStopExtraction) {
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

            if (mExtractedUrlStack.size() > 0 && !mShouldStopExtraction) {
                mLastPriorityUrl = mExtractedUrlStack.poll();

                Timber.d("Priority : %s", mLastPriorityUrl);

                List<Bundle> possibleUrls = new ArrayList<>();

                for (String url : mExtractedUrlStack) {
                    if (url == null) continue;

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(CustomTabsService.KEY_URL, Uri.parse(url));
                    possibleUrls.add(bundle);
                    Timber.d("Others : %s", url);
                }

                boolean success;
                if (mLastPriorityUrl != null) {
                    if (!mLastPriorityUrl.equalsIgnoreCase(mLastFetchedUrl)) {
                        success = mCustomActivityHelper.mayLaunchUrl(Uri.parse(mLastPriorityUrl), null, possibleUrls);
                        if (success) mLastFetchedUrl = mLastPriorityUrl;
                    } else {
                        Timber.d("Ignored, already fetched");
                    }
                }
            }
            mExtractedUrlStack.clear();
        } catch (Exception e) {
            emptyStacks();
            e.printStackTrace();
        }
    }

    private void emptyStacks() {
        mTreeTraversingStack.clear();
        mExtractedUrlStack.clear();
    }

    private boolean shouldIgnoreEvent(AccessibilityEvent event) {
        if (!Preferences.preFetch(this)) return true;
        if (!isWifiConditionsMet()) return true;
        String packageName = "";
        if (event.getPackageName() != null) {
            packageName = event.getPackageName().toString();
            return packageName.equalsIgnoreCase(Preferences.customTabApp(this));
        }
        return false;
    }

    private void processNode(@NonNull AccessibilityNodeInfo node) {
        if (node.getText() != null) {
            extractURL(node.getText().toString());
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
            mExtractedUrlStack.add(url);
            if (mExtractedUrlStack.size() == 1 && url.equalsIgnoreCase(mLastPriorityUrl)) {
                // This means the new extraction is giving the same urls as last extraction did.
                // In this case, we will explicitly stop the tree traversal by setting a flag variable.
                Timber.d("Encountered same priority url twice, stopping extraction");
                mShouldStopExtraction = true;
            }
        }
    }


    private void stopWarmUpService() {
        try {
            stopService(new Intent(this, WarmupService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isWifiConditionsMet() {
        if (Preferences.wifiOnlyPrefetch(this)) {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            // TODO fix this deprecated call
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return mWifi.isConnected();
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
        if (mCustomActivityHelper != null)
            mCustomActivityHelper.unbindCustomTabsService(this);
        emptyStacks();
        super.onDestroy();
    }
}
